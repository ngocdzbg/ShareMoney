package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.response.DebtTransferResponse;
import com.ngocnb.sharemoney.dto.response.MemberBalanceResponse;
import com.ngocnb.sharemoney.dto.response.PaymentResponse;
import com.ngocnb.sharemoney.dto.response.SettlementResponse;
import com.ngocnb.sharemoney.entity.*;
import com.ngocnb.sharemoney.exception.ResourceNotFoundException;
import com.ngocnb.sharemoney.repository.*;
import com.ngocnb.sharemoney.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {
    private final GroupNameRepository groupNameRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public SettlementResponse calculateSettlement(Long groupId) {
        // 1. Kiểm tra nhóm tồn tại
        if (!groupNameRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with id: " + groupId);
        }

        // 2. Lấy toàn bộ danh sách thành viên và tất cả khoản chi của nhóm
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findAllByGroupIdFetchDetails(groupId);

        // Khởi tạo bảng theo dõi thu/chi cho từng thành viên
        Map<Long, BigDecimal> totalPaidMap = new HashMap<>();
        Map<Long, BigDecimal> totalExpenseMap = new HashMap<>();
        Map<Long, GroupMember> memberMap = new HashMap<>();

        for (GroupMember member : members) {
            totalPaidMap.put(member.getId(), BigDecimal.ZERO);
            totalExpenseMap.put(member.getId(), BigDecimal.ZERO);
            memberMap.put(member.getId(), member);
        }

        BigDecimal totalGroupExpense = BigDecimal.ZERO;

        // 3. Tính tổng tiền đã trả (Paid) và tổng tiền tiêu dùng (Expense)
        for (Expense expense : expenses) {
            totalGroupExpense = totalGroupExpense.add(expense.getAmount());

            // Cộng tiền người đứng ra trả
            Long paidById = expense.getPaidBy().getId();
            if (totalPaidMap.containsKey(paidById)) {
                totalPaidMap.put(paidById, totalPaidMap.get(paidById).add(expense.getAmount()));
            }

            // Cộng tiền phần chi tiêu của từng người tham gia
            if (expense.getParticipants() != null) {
                for (ExpenseParticipant participant : expense.getParticipants()) {
                    Long participantId = participant.getGroupMember().getId();
                    if (totalExpenseMap.containsKey(participantId)) {
                        totalExpenseMap.put(participantId,
                                totalExpenseMap.get(participantId).add(participant.getShareAmount()));
                    }
                }
            }
        }

        // 4. Tính Net Balance cho từng thành viên
        List<MemberBalanceResponse> balanceList = new ArrayList<>();
        Map<Long, BigDecimal> netBalanceMap = new HashMap<>();

        for (GroupMember member : members) {
            Long mId = member.getId();
            BigDecimal paid = totalPaidMap.get(mId);
            BigDecimal exp = totalExpenseMap.get(mId);
            BigDecimal net = paid.subtract(exp); // Positive = Chủ nợ, Negative = Con nợ

            netBalanceMap.put(mId, net);
            balanceList.add(MemberBalanceResponse.builder()
                    .memberId(mId)
                    .memberName(member.getName())
                    .totalPaid(paid)
                    .totalExpense(exp)
                    .netBalance(net)
                    .build());
        }

        // 5. Chạy thuật toán thu gọn giao dịch (Minimizing Cash Flow)
        List<DebtTransferResponse> transfers = simplifyDebts(netBalanceMap, memberMap);

        return SettlementResponse.builder()
                .groupId(groupId)
                .totalGroupExpense(totalGroupExpense)
                .memberBalances(balanceList)
                .transfers(transfers)
                .build();
    }


    /**
     * Thuật toán Tham lam (Greedy Two-Pointer) giải quyết bài toán bù trừ nợ tối ưu
     */
    private List<DebtTransferResponse> simplifyDebts(
            Map<Long, BigDecimal> netBalanceMap,
            Map<Long, GroupMember> memberMap
    ) {
        List<DebtTransferResponse> transfers = new ArrayList<>();

        // Danh sách con nợ (Net < 0) và chủ nợ (Net > 0)
        List<Map.Entry<Long, BigDecimal>> debtors = new ArrayList<>();
        List<Map.Entry<Long, BigDecimal>> creditors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : netBalanceMap.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().abs()));
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }

        int i = 0; // Con nợ
        int j = 0; // Chủ nợ

        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<Long, BigDecimal> debtor = debtors.get(i);
            Map.Entry<Long, BigDecimal> creditor = creditors.get(j);

            BigDecimal debtAmount = debtor.getValue();
            BigDecimal creditAmount = creditor.getValue();

            // Số tiền chuyển bằng MIN(tiền nợ, tiền được đòi)
            BigDecimal minAmount = debtAmount.min(creditAmount).setScale(2, RoundingMode.HALF_UP);

            if (minAmount.compareTo(BigDecimal.ZERO) > 0) {
                GroupMember fromMember = memberMap.get(debtor.getKey());
                GroupMember toMember = memberMap.get(creditor.getKey());

                transfers.add(DebtTransferResponse.builder()
                        .fromMemberId(fromMember.getId())
                        .fromMemberName(fromMember.getName())
                        .toMemberId(toMember.getId())
                        .toMemberName(toMember.getName())
                        .amount(minAmount)
                        .build());
            }

            // Cập nhật lại số tiền còn lại
            debtor.setValue(debtAmount.subtract(minAmount));
            creditor.setValue(creditAmount.subtract(minAmount));

            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }

        return transfers;
    }


    /**
     * CHỐT SỔ: Tính toán các khoản nợ rút gọn và LƯU VÀO DB
     */
    @Override
    @Transactional
    public List<PaymentResponse> finalizeSettlement(Long groupId, String note) {
        // 1. Kiểm tra nhóm tồn tại
        GroupName group = groupNameRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // 2. Tính toán kết quả quyết toán nợ ở thời điểm chốt sổ
        SettlementResponse settlement = calculateSettlement(groupId);

        // 3. Xóa các bản ghi Payment cũ chưa hoàn thành của nhóm này (nếu chốt sổ lại)
        paymentRepository.deleteByGroupId(groupId);

        // 4. Chuyển đổi các giao dịch đề xuất (transfers) thành entity Payment để lưu DB
        List<Payment> paymentsToSave = new ArrayList<>();

        for (DebtTransferResponse transfer : settlement.getTransfers()) {
            GroupMember fromMember = groupMemberRepository.findById(transfer.getFromMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + transfer.getFromMemberId()));
            GroupMember toMember = groupMemberRepository.findById(transfer.getToMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + transfer.getToMemberId()));

            Payment payment = Payment.builder()
                    .group(group)
                    .fromMember(fromMember)
                    .toMember(toMember)
                    .amount(transfer.getAmount())
                    .status(PaymentStatus.PENDING) // Mặc định là PENDING chờ các thành viên trả tiền
                    .note(note != null ? note : "Chốt sổ quyết toán nhóm " + group.getName())
                    .build();

            paymentsToSave.add(payment);
        }

        // 5. Lưu vào DB
        List<Payment> savedPayments = paymentRepository.saveAll(paymentsToSave);
        log.info("✓ Finalized settlement for group {}. Saved {} payments to DB.", groupId, savedPayments.size());

        // 6. Map sang DTO trả về
        return savedPayments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .groupId(payment.getGroup().getId())
                .fromMemberId(payment.getFromMember().getId())
                .fromMemberName(payment.getFromMember().getName())
                .toMemberId(payment.getToMember().getId())
                .toMemberName(payment.getToMember().getName())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .note(payment.getNote())
                .build();
    }
}
