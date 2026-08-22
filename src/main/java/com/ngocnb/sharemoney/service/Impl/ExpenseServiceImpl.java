package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseParticipantResponse;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import com.ngocnb.sharemoney.entity.*;
import com.ngocnb.sharemoney.exception.BadRequestException;
import com.ngocnb.sharemoney.exception.ResourceNotFoundException;
import com.ngocnb.sharemoney.repository.ExpenseRepository;
import com.ngocnb.sharemoney.repository.GroupMemberRepository;
import com.ngocnb.sharemoney.repository.GroupNameRepository;
import com.ngocnb.sharemoney.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupNameRepository groupNameRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    @Transactional
    public ExpenseResponse createExpense(Long groupId, ExpenseCreateRequest request) {
        log.info("Service: Creating expense '{}' in group ID: {}", request.getDescription(), groupId);

        GroupName group = groupNameRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // 1. Kiểm tra người trả tiền (paidBy) có thuộc group không
        GroupMember paidBy = groupMemberRepository.findByIdAndGroupId(request.getPaidById(), groupId)
                .orElseThrow(() -> new ResourceNotFoundException("PaidBy member not found with id: " + request.getPaidById() + " in group: " + groupId));

        // 2. Validate và lấy thông tin các thành viên tham gia chia tiền
        Map<Long, GroupMember> memberMap = validateAndGetMembers(groupId, request.getParticipantIds());

        // 3. Khởi tạo đối tượng Expense
        Expense expense = Expense.builder()
                .group(group)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paidBy(paidBy)
                .participants(new ArrayList<>())
                .build();

        // 4. Chia đều tiền cho danh sách người tham gia
        List<ExpenseParticipant> participants = calculateEqualShares(expense, request.getAmount(), request.getParticipantIds(), memberMap);
        expense.getParticipants().addAll(participants);

        Expense savedExpense = expenseRepository.save(expense);
        return mapToResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long groupId, Long expenseId) {
        log.info("Service: Fetching expense ID: {} in group ID: {}", expenseId, groupId);

        Expense expense = expenseRepository.findByIdAndGroupIdFetchDetails(expenseId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId + " in group: " + groupId));

        return mapToResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpensesByGroupId(Long groupId, int page, int size, String sortBy, String direction) {
        log.info("Service: Fetching expenses for group ID: {}", groupId);

        if (!groupNameRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with id: " + groupId);
        }

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, 30), sort);
        Page<Expense> expensePage = expenseRepository.findByGroupIdFetchDetails(groupId, pageable);

        return expensePage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long groupId, Long expenseId, ExpenseUpdateRequest request) {
        log.info("Service: Updating expense ID: {} in group ID: {}", expenseId, groupId);

        Expense expense = expenseRepository.findByIdAndGroupIdFetchDetails(expenseId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId + " in group: " + groupId));

        GroupMember paidBy = groupMemberRepository.findByIdAndGroupId(request.getPaidById(), groupId)
                .orElseThrow(() -> new ResourceNotFoundException("PaidBy member not found with id: " + request.getPaidById() + " in group: " + groupId));

        Map<Long, GroupMember> memberMap = validateAndGetMembers(groupId, request.getParticipantIds());

        // Cập nhật thông tin cơ bản
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaidBy(paidBy);

        // Xóa danh sách participant cũ (orphanRemoval = true sẽ tự xóa dưới DB)
        expense.getParticipants().clear();
        expenseRepository.flush();

        // Tính toán chia tiền lại từ đầu
        List<ExpenseParticipant> updatedParticipants = calculateEqualShares(expense, request.getAmount(), request.getParticipantIds(), memberMap);
        expense.getParticipants().addAll(updatedParticipants);

        Expense savedExpense = expenseRepository.save(expense);
        return mapToResponse(savedExpense);
    }

    @Override
    @Transactional
    public void deleteExpense(Long groupId, Long expenseId) {
        log.info("Service: Deleting expense ID: {} from group ID: {}", expenseId, groupId);

        Expense expense = expenseRepository.findByIdAndGroupIdFetchDetails(expenseId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId + " in group: " + groupId));

        expenseRepository.delete(expense);
    }

    /**
     * Chia đều số tiền cho tất cả người tham gia.
     * ShareAmount của tất cả người là như nhau và giữ ở hàng đơn vị.
     * <p>
     * Ví dụ:
     * 100 / 3 = 33
     * 33 x 3 = 99
     */
    private List<ExpenseParticipant> calculateEqualShares(
            Expense expense,
            BigDecimal totalAmount,
            List<Long> participantIds,
            Map<Long, GroupMember> memberMap
    ) {
        int count = participantIds.size();
        BigDecimal countBD = BigDecimal.valueOf(count);

        // Chia đều và chỉ giữ phần nguyên (hàng đơn vị)
        BigDecimal shareAmount = totalAmount.divide(countBD, 0, RoundingMode.DOWN);

        List<ExpenseParticipant> participants = new ArrayList<>();

        for (Long memberId : participantIds) {
            GroupMember member = memberMap.get(memberId);

            ExpenseParticipant participant = ExpenseParticipant.builder()
                    .expense(expense)
                    .groupMember(member)
                    .shareAmount(shareAmount)
                    .build();

            participants.add(participant);
        }

        return participants;
    }

    private Map<Long, GroupMember> validateAndGetMembers(Long groupId, List<Long> participantIds) {
        Set<Long> uniqueIds = Set.copyOf(participantIds);

        List<GroupMember> members = groupMemberRepository.findAllById(uniqueIds);

        // Kiểm tra xem tất cả ID gửi lên có tồn tại trong DB không
        if (members.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("One or more participant IDs were not found");
        }

        // Kiểm tra tất cả member phải thuộc đúng groupId này
        boolean allInGroup = members.stream().allMatch(m -> m.getGroup().getId().equals(groupId));
        if (!allInGroup) {
            throw new BadRequestException("All participants must belong to group ID: " + groupId);
        }

        return members.stream().collect(Collectors.toMap(GroupMember::getId, Function.identity()));
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        List<ExpenseParticipantResponse> participantResponses = expense.getParticipants().stream()
                .map(p -> ExpenseParticipantResponse.builder()
                        .id(p.getId())
                        .memberId(p.getGroupMember().getId())
                        .memberName(p.getGroupMember().getName())
                        .shareAmount(p.getShareAmount())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .paidByMemberId(expense.getPaidBy().getId())
                .paidByMemberName(expense.getPaidBy().getName())
                .participants(participantResponses)
                .build();
    }
}