package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import com.ngocnb.sharemoney.entity.*;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupNameRepository groupNameRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * Lấy danh sách expense có phân trang và sắp xếp.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAllExpenses(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        log.debug(
                "Fetching expenses - page: {}, size: {}, sortBy: {}, direction: {}",
                page,
                size,
                sortBy,
                direction
        );

        String[] validSortFields = {
                "id",
                "description",
                "amount",
                "expenseDate"
        };

        boolean isValidField =
                java.util.Arrays.asList(validSortFields)
                        .contains(sortBy);

        if (!isValidField) {
            log.warn("Invalid sortBy field: {}", sortBy);

            throw new IllegalArgumentException(
                    String.format(
                            "Invalid sortBy field: %s. Valid fields: %s",
                            sortBy,
                            String.join(", ", validSortFields)
                    )
            );
        }

        Sort.Direction sortDirection =
                "DESC".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Expense> expenses =
                expenseRepository.findAll(pageable);

        log.info(
                "Found {} expenses (total: {})",
                expenses.getNumberOfElements(),
                expenses.getTotalElements()
        );

        return expenses.map(this::convertToResponse);
    }

    /**
     * Lấy expense theo ID.
     */
    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {

        log.debug("Fetching expense with id: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Expense not found with id: {}", id);

                    return new ResourceNotFoundException(
                            "Expense với ID " + id + " không tồn tại"
                    );
                });

        return convertToResponse(expense);
    }

    /**
     * Tạo expense mới.
     */
    @Override
    @Transactional
    public ExpenseResponse createExpense(
            ExpenseCreateRequest request
    ) {

        log.debug(
                "Creating expense: {}",
                request.getDescription()
        );

        /*
         * 1. Kiểm tra Group
         */
        GroupName group = groupNameRepository.findById(
                request.getGroupId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Group với ID "
                                + request.getGroupId()
                                + " không tồn tại"
                )
        );

        /*
         * 3. Kiểm tra danh sách participants
         */
        if (request.getParticipantIds() == null
                || request.getParticipantIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "Expense phải có ít nhất một người tham gia"
            );
        }

        /*
         * 4. Tạo Expense
         */
        Expense expense = Expense.builder()
                .group(group)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paidBy(request.getPaidBy())
                .participants(new ArrayList<>())
                .build();

        /*
         * 5. Lưu Expense trước để có ID
         */
        Expense savedExpense =
                expenseRepository.save(expense);

        /*
         * 6. Tạo ExpenseParticipant
         */
        createParticipants(
                savedExpense,
                request.getParticipantIds()
        );

        log.info(
                "Expense created successfully - id: {}, description: {}, amount: {}",
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getAmount()
        );

        return convertToResponse(savedExpense);
    }

    /**
     * Cập nhật Expense bằng PATCH.
     */
    @Override
    @Transactional
    public ExpenseResponse updateExpense(
            Long id,
            ExpenseUpdateRequest request
    ) {
        log.debug("Updating expense with id: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Expense not found with id: {} for update", id);

                    return new ResourceNotFoundException(
                            "Expense với ID " + id + " không tồn tại"
                    );
                });

        /*
         * Kiểm tra amount có thay đổi không.
         */
        boolean amountChanged = request.getAmount() != null
                && request.getAmount().compareTo(expense.getAmount()) != 0;

        /*
         * Kiểm tra participantIds có được gửi lên không.
         *
         * null  -> không thay đổi participants
         * khác null -> thay đổi participants
         */
        boolean participantsChanged =
                request.getParticipantIds() != null;

        /*
         * Update group
         */
        if (request.getGroupId() != null) {

            GroupName group = groupNameRepository.findById(
                    request.getGroupId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Group với ID "
                                    + request.getGroupId()
                                    + " không tồn tại"
                    )
            );

            expense.setGroup(group);

            log.debug(
                    "Expense {} group changed to {}",
                    id,
                    request.getGroupId()
            );
        }

        /*
         * Update description
         */
        if (request.getDescription() != null) {
            expense.setDescription(
                    request.getDescription()
            );
        }

        /*
         * Update amount
         */
        if (amountChanged) {

            log.debug(
                    "Expense {} amount changed from {} to {}",
                    id,
                    expense.getAmount(),
                    request.getAmount()
            );

            expense.setAmount(
                    request.getAmount()
            );
        }

        /*
         * Update expense date
         */
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(
                    request.getExpenseDate()
            );
        }

        /*
         * Update paidBy
         */
        if (request.getPaidBy() != null) {
            expense.setPaidBy(request.getPaidBy());
        }

        /*
         * Nếu amount hoặc participants thay đổi
         * thì phải tính lại shareAmount.
         */
        if (amountChanged || participantsChanged) {

            /*
             * Nếu request có participantIds
             * thì dùng danh sách mới.
             *
             * Nếu chỉ amount thay đổi thì giữ
             * nguyên danh sách participant hiện tại.
             */
            List<Long> participantIds;

            if (participantsChanged) {

                participantIds = request.getParticipantIds();

                if (participantIds == null
                        || participantIds.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Expense phải có ít nhất một người tham gia"
                    );
                }

            } else {
                participantIds = expense.getParticipants()
                        .stream()
                        .map(participant -> participant.getGroupMember().getId())
                        .toList();
            }

            /*
             * Xóa participants cũ.
             */
            expense.getParticipants().clear();

            /*
             * Tạo lại participants với
             * shareAmount mới.
             */
            createParticipants(
                    expense,
                    participantIds
            );

            log.debug(
                    "Recalculated participants for expense {}",
                    id
            );
        }

        Expense updatedExpense =
                expenseRepository.save(expense);

        log.info(
                "Expense updated successfully - id: {}",
                updatedExpense.getId()
        );

        return convertToResponse(updatedExpense);
    }

    /**
     * Xóa expense.
     */
    @Override
    @Transactional
    public void deleteExpenseById(Long id) {

        log.debug("Deleting expense with id: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Expense not found with id: {} for deletion",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Expense với ID "
                                    + id
                                    + " không tồn tại"
                    );
                });

        expenseRepository.delete(expense);

        log.info(
                "Expense deleted successfully with id: {}",
                id
        );
    }

    /**
     * Tạo danh sách ExpenseParticipant.
     */
    private void createParticipants(
            Expense expense,
            List<Long> participantIds
    ) {

        /*
         * Kiểm tra ID trùng nhau.
         *
         * Ví dụ:
         * [1, 2, 2, 3]
         *
         * sẽ bị reject.
         */
        List<Long> uniqueParticipantIds =
                participantIds.stream()
                        .distinct()
                        .toList();

        if (uniqueParticipantIds.size() != participantIds.size()) {
            throw new IllegalArgumentException(
                    "participantIds không được chứa GroupMember trùng nhau"
            );
        }

        /*
         * Expense bắt buộc phải thuộc một Group.
         */
        if (expense.getGroup() == null) {
            throw new IllegalStateException(
                    "Expense chưa có Group"
            );
        }

        /*
         * Tính số tiền mỗi người phải chịu.
         *
         * Ví dụ:
         * 100 / 3
         *
         * shareAmount = 33.33
         */
        BigDecimal shareAmount =
                expense.getAmount()
                        .divide(
                                BigDecimal.valueOf(
                                        uniqueParticipantIds.size()
                                ),
                                2,
                                RoundingMode.DOWN
                        );

        /*
         * Tính tổng tiền sau khi chia.
         */
        BigDecimal totalShare =
                shareAmount.multiply(
                        BigDecimal.valueOf(
                                uniqueParticipantIds.size()
                        )
                );

        /*
         * Phần tiền dư do làm tròn.
         *
         * Ví dụ:
         * 100 / 3
         *
         * 33.33 * 3 = 99.99
         *
         * remainder = 0.01
         */
        BigDecimal remainder =
                expense.getAmount()
                        .subtract(totalShare);

        for (int i = 0; i < uniqueParticipantIds.size(); i++) {

            Long groupMemberId =
                    uniqueParticipantIds.get(i);

            /*
             * Tìm GroupMember.
             */
            GroupMember groupMember =
                    groupMemberRepository.findById(groupMemberId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "GroupMember với ID "
                                                    + groupMemberId
                                                    + " không tồn tại"
                                    )
                            );

            /*
             * GroupMember phải thuộc Group
             * của Expense.
             */
            if (!groupMember.getGroup().getId()
                    .equals(expense.getGroup().getId())) {

                throw new IllegalArgumentException(
                        "GroupMember với ID "
                                + groupMemberId
                                + " không thuộc Group của Expense"
                );
            }

            /*
             * Người cuối cùng nhận phần tiền dư.
             */
            BigDecimal finalShare =
                    shareAmount;

            if (i == uniqueParticipantIds.size() - 1) {
                finalShare =
                        finalShare.add(remainder);
            }

            ExpenseParticipant participant =
                    ExpenseParticipant.builder()
                            .expense(expense)
                            .groupMember(groupMember)
                            .shareAmount(finalShare)
                            .build();

            expense.getParticipants()
                    .add(participant);
        }
    }

    /**
     * Convert Expense Entity -> ExpenseResponse.
     */
    private ExpenseResponse convertToResponse(
            Expense expense
    ) {

        List<Long> participantIds =
                expense.getParticipants()
                        .stream()
                        .map(participant ->
                                participant.getGroupMember().getId()
                        )
                        .toList();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .paidBy(expense.getPaidBy().getId())
                .participantIds(participantIds)
                .build();
    }
}