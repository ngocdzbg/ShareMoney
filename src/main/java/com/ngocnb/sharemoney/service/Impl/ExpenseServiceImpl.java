package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseParticipantResponse;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import com.ngocnb.sharemoney.entity.Expense;
import com.ngocnb.sharemoney.entity.ExpenseParticipant;
import com.ngocnb.sharemoney.entity.GroupMember;
import com.ngocnb.sharemoney.entity.GroupName;
import com.ngocnb.sharemoney.exception.BadRequestException;
import com.ngocnb.sharemoney.exception.ResourceNotFoundException;
import com.ngocnb.sharemoney.repository.ExpenseRepository;
import com.ngocnb.sharemoney.repository.GroupMemberRepository;
import com.ngocnb.sharemoney.repository.GroupNameRepository;
import com.ngocnb.sharemoney.service.ExpenseService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    public ExpenseResponse createExpense(
            Long groupId,
            ExpenseCreateRequest request
    ) {
        log.info(
                "Service: Creating expense '{}' in group ID: {}",
                request.getDescription(),
                groupId
        );

        GroupName group = groupNameRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId
                        )
                );

        // 1. Kiểm tra người trả tiền
        GroupMember paidBy = groupMemberRepository
                .findByIdAndGroupId(
                        request.getPaidById(),
                        groupId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "PaidBy member not found with id: "
                                        + request.getPaidById()
                                        + " in group: "
                                        + groupId
                        )
                );

        // 2. Validate participants
        Map<Long, GroupMember> memberMap =
                validateAndGetMembers(
                        groupId,
                        request.getParticipantIds()
                );

        // 3. Tạo Expense
        Expense expense = Expense.builder()
                .group(group)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paidBy(paidBy)
                .participants(new ArrayList<>())
                .build();

        // 4. Tạo participants
        List<ExpenseParticipant> participants =
                createEqualShareParticipants(
                        expense,
                        request.getAmount(),
                        request.getParticipantIds(),
                        memberMap
                );

        expense.getParticipants().addAll(participants);

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(
            Long groupId,
            Long expenseId
    ) {
        log.info(
                "Service: Fetching expense ID: {} in group ID: {}",
                expenseId,
                groupId
        );

        Expense expense = expenseRepository
                .findByIdAndGroupIdFetchDetails(
                        expenseId,
                        groupId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found with id: "
                                        + expenseId
                                        + " in group: "
                                        + groupId
                        )
                );

        return mapToResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpensesByGroupId(
            Long groupId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        log.info(
                "Service: Fetching expenses for group ID: {}",
                groupId
        );

        if (!groupNameRepository.existsById(groupId)) {
            throw new ResourceNotFoundException(
                    "Group not found with id: " + groupId
            );
        }

        Sort sort = direction.equalsIgnoreCase(
                Sort.Direction.ASC.name()
        )
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 30),
                sort
        );

        // Query 1:
        // Lấy Expense theo pagination ở database
        Page<Expense> expensePage =
                expenseRepository.findByGroupId(
                        groupId,
                        pageable
                );

        if (expensePage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Lấy danh sách Expense ID của page hiện tại
        List<Long> expenseIds =
                expensePage.getContent()
                        .stream()
                        .map(Expense::getId)
                        .toList();

        // Query 2:
        // Fetch participants + groupMember
        List<Expense> expensesWithDetails =
                expenseRepository.findAllByIdInFetchDetails(
                        expenseIds
                );

        // Map để giữ đúng thứ tự pagination
        Map<Long, Expense> expenseMap =
                expensesWithDetails.stream()
                        .collect(Collectors.toMap(
                                Expense::getId,
                                expense -> expense
                        ));

        // Rebuild response theo đúng thứ tự của Query 1
        List<ExpenseResponse> responses =
                expensePage.getContent()
                        .stream()
                        .map(expense -> expenseMap.get(expense.getId()))
                        .map(this::mapToResponse)
                        .toList();

        return new PageImpl<>(
                responses,
                pageable,
                expensePage.getTotalElements()
        );
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(
            Long groupId,
            Long expenseId,
            ExpenseUpdateRequest request
    ) {
        log.info(
                "Service: Updating expense ID: {} in group ID: {}",
                expenseId,
                groupId
        );

        // ============================================================
        // 1. Lock Expense bằng PESSIMISTIC_WRITE
        // ============================================================

        Expense expense = expenseRepository
                .findByIdAndGroupIdFetchDetails(
                        expenseId,
                        groupId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found with id: "
                                        + expenseId
                                        + " in group: "
                                        + groupId
                        )
                );

        // ============================================================
        // 2. Optimistic Lock check
        // ============================================================

        if (!expense.getVersion().equals(request.getVersion())) {

            log.warn(
                    "Optimistic lock conflict. Expense ID: {}, " +
                            "Request version: {}, DB version: {}",
                    expenseId,
                    request.getVersion(),
                    expense.getVersion()
            );

            throw new OptimisticLockException(
                    "Expense has been modified by another request"
            );
        }

        // ============================================================
        // 3. Validate PaidBy
        // ============================================================

        GroupMember paidBy = groupMemberRepository
                .findByIdAndGroupId(
                        request.getPaidById(),
                        groupId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "PaidBy member not found with id: "
                                        + request.getPaidById()
                                        + " in group: "
                                        + groupId
                        )
                );

        // ============================================================
        // 4. Validate participants
        // ============================================================

        Map<Long, GroupMember> memberMap =
                validateAndGetMembers(
                        groupId,
                        request.getParticipantIds()
                );

        // ============================================================
        // 5. Update thông tin Expense
        // ============================================================

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaidBy(paidBy);

        // ============================================================
        // 6. Validate duplicate participant IDs
        // ============================================================

        List<Long> participantIds =
                request.getParticipantIds();

        Set<Long> newParticipantIds =
                new LinkedHashSet<>(participantIds);

        if (newParticipantIds.size() != participantIds.size()) {
            throw new BadRequestException(
                    "Participant IDs must be unique"
            );
        }

        // ============================================================
        // 7. Xóa participant không còn tồn tại
        // ============================================================

        expense.getParticipants().removeIf(
                participant ->
                        !newParticipantIds.contains(
                                participant
                                        .getGroupMember()
                                        .getId()
                        )
        );

        /*
         * orphanRemoval = true
         *
         * Flush DELETE participant cũ xuống DB trước.
         *
         * Điều này tránh:
         *
         * INSERT (expense_id=3, group_member_id=2)
         *
         * trong khi DB vẫn còn:
         *
         * (expense_id=3, group_member_id=2)
         */
        expenseRepository.flush();

        // ============================================================
        // 8. Lấy danh sách participant hiện tại
        // ============================================================

        Set<Long> existingMemberIds =
                expense.getParticipants()
                        .stream()
                        .map(participant ->
                                participant
                                        .getGroupMember()
                                        .getId()
                        )
                        .collect(Collectors.toSet());

        // ============================================================
        // 9. Thêm participant mới
        // ============================================================

        for (Long memberId : newParticipantIds) {

            if (!existingMemberIds.contains(memberId)) {

                GroupMember member =
                        memberMap.get(memberId);

                ExpenseParticipant participant =
                        ExpenseParticipant.builder()
                                .expense(expense)
                                .groupMember(member)
                                .build();

                expense.getParticipants().add(participant);
            }
        }

        // ============================================================
        // 10. Tính lại shareAmount
        // ============================================================

        calculateEqualShares(
                expense.getParticipants(),
                request.getAmount()
        );

        // ============================================================
        // 11. Save
        // ============================================================

        Expense savedExpense =
                expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    @Override
    @Transactional
    public void deleteExpense(
            Long groupId,
            Long expenseId
    ) {
        log.info(
                "Service: Deleting expense ID: {} from group ID: {}",
                expenseId,
                groupId
        );

        Expense expense = expenseRepository
                .findByIdAndGroupIdFetchDetails(
                        expenseId,
                        groupId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found with id: "
                                        + expenseId
                                        + " in group: "
                                        + groupId
                        )
                );

        expenseRepository.delete(expense);
    }

    /**
     * Tạo danh sách ExpenseParticipant khi CREATE Expense.
     * <p>
     * ShareAmount của tất cả người tham gia bằng nhau.
     * <p>
     * Chỉ giữ phần nguyên ở hàng đơn vị.
     * <p>
     * Ví dụ:
     * <p>
     * 100 / 3 = 33
     * <p>
     * Người 1 = 33
     * Người 2 = 33
     * Người 3 = 33
     * <p>
     * Tổng = 99
     */
    private List<ExpenseParticipant> createEqualShareParticipants(
            Expense expense,
            BigDecimal totalAmount,
            List<Long> participantIds,
            Map<Long, GroupMember> memberMap
    ) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new BadRequestException(
                    "At least one participant is required"
            );
        }

        int count = participantIds.size();

        BigDecimal shareAmount = totalAmount.divide(
                BigDecimal.valueOf(count),
                0,
                RoundingMode.DOWN
        );

        List<ExpenseParticipant> participants =
                new ArrayList<>();

        for (Long memberId : participantIds) {

            GroupMember member =
                    memberMap.get(memberId);

            ExpenseParticipant participant =
                    ExpenseParticipant.builder()
                            .expense(expense)
                            .groupMember(member)
                            .shareAmount(shareAmount)
                            .build();

            participants.add(participant);
        }

        return participants;
    }

    /**
     * Tính lại shareAmount cho các ExpenseParticipant hiện tại.
     * <p>
     * Hàm này KHÔNG tạo ExpenseParticipant mới.
     * Chỉ cập nhật shareAmount trên entity đang tồn tại.
     * <p>
     * Ví dụ:
     * <p>
     * 100 / 3 = 33
     * <p>
     * participant 1 = 33
     * participant 2 = 33
     * participant 3 = 33
     */
    private void calculateEqualShares(
            List<ExpenseParticipant> participants,
            BigDecimal totalAmount
    ) {
        if (participants == null || participants.isEmpty()) {
            return;
        }

        int count = participants.size();

        BigDecimal shareAmount = totalAmount.divide(
                BigDecimal.valueOf(count),
                0,
                RoundingMode.DOWN
        );

        for (ExpenseParticipant participant : participants) {
            participant.setShareAmount(shareAmount);
        }
    }

    /**
     * Validate participant IDs và lấy GroupMember tương ứng.
     */
    private Map<Long, GroupMember> validateAndGetMembers(
            Long groupId,
            List<Long> participantIds
    ) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new BadRequestException(
                    "At least one participant is required"
            );
        }

        /*
         * Dùng LinkedHashSet để:
         * - loại bỏ duplicate khi kiểm tra
         * - giữ nguyên thứ tự participantIds
         */
        Set<Long> uniqueIds =
                new LinkedHashSet<>(participantIds);

        // Nếu request có ID trùng nhau thì báo lỗi
        if (uniqueIds.size() != participantIds.size()) {
            throw new BadRequestException(
                    "Participant IDs must be unique"
            );
        }

        List<GroupMember> members =
                groupMemberRepository.findAllById(uniqueIds);

        // Kiểm tra tất cả ID có tồn tại
        if (members.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more participant IDs were not found"
            );
        }

        // Kiểm tra tất cả member thuộc đúng group
        boolean allInGroup = members.stream()
                .allMatch(member ->
                        member.getGroup()
                                .getId()
                                .equals(groupId)
                );

        if (!allInGroup) {
            throw new BadRequestException(
                    "All participants must belong to group ID: "
                            + groupId
            );
        }

        return members.stream()
                .collect(Collectors.toMap(
                        GroupMember::getId,
                        Function.identity()
                ));
    }

    /**
     * Mapping Entity -> Response DTO.
     */
    private ExpenseResponse mapToResponse(
            Expense expense
    ) {
        List<ExpenseParticipantResponse> participantResponses =
                expense.getParticipants()
                        .stream()
                        .map(p ->
                                ExpenseParticipantResponse.builder()
                                        .id(p.getId())
                                        .memberId(
                                                p.getGroupMember().getId()
                                        )
                                        .memberName(
                                                p.getGroupMember().getName()
                                        )
                                        .shareAmount(
                                                p.getShareAmount()
                                        )
                                        .build()
                        )
                        .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .version(expense.getVersion())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .paidByMemberId(expense.getPaidBy().getId())
                .paidByMemberName(expense.getPaidBy().getName())
                .participants(participantResponses)
                .build();
    }
}