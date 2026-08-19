package com.ngocnb.sharemoney.service.Impl;


import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import com.ngocnb.sharemoney.entity.Expense;
import com.ngocnb.sharemoney.entity.Group;
import com.ngocnb.sharemoney.entity.User;
import com.ngocnb.sharemoney.exception.ResourceNotFoundException;
import com.ngocnb.sharemoney.repository.ExpenseRepository;
import com.ngocnb.sharemoney.repository.GroupRepository;
import com.ngocnb.sharemoney.repository.UserRepository;
import com.ngocnb.sharemoney.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách expense có phân trang và sắp xếp.
     */
    @Override
    public Page<ExpenseResponse> getAllExpenses(int page, int size, String sortBy, String direction) {
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
    public ExpenseResponse getExpenseById(Long id) {
        log.debug("Fetching expense with id: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Expense not found with id: {}", id);

                    return new ResourceNotFoundException("Expense với ID " + id + " không tồn tại");
                });

        return convertToResponse(expense);
    }

    /**
     * Tạo expense mới.
     */
    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseCreateRequest request) {
        log.debug("Creating expense: {}", request.getDescription());

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Group với ID " + request.getGroupId() + " không tồn tại")
                );

        User user = userRepository.findById(request.getPaidBy())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User với ID " + request.getPaidBy() + " không tồn tại")
                );

        Expense expense = Expense.builder()
                .group(group)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paidBy(user)
                .build();

        Expense savedExpense =
                expenseRepository.save(expense);

        log.info(
                "Expense created successfully - id: {}, description: {}, amount: {}",
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getAmount()
        );

        return convertToResponse(savedExpense);
    }

    /**
     * Cập nhật expense.
     */
    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseUpdateRequest request) {
        log.debug("Updating expense with id: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Expense not found with id: {} for update", id);

                    return new ResourceNotFoundException("Expense với ID " + id + " không tồn tại");
                });

        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Group với ID " + request.getGroupId() + " không tồn tại")
                    );

            expense.setGroup(group);
        }

        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }

        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }

        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }

        if (request.getPaidBy() != null) {

            User user = userRepository.findById(request.getPaidBy())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User với ID " + request.getPaidBy() + " không tồn tại")
                    );

            expense.setPaidBy(user);
        }

        Expense updatedExpense = expenseRepository.save(expense);

        log.info("Expense updated successfully - id: {}", updatedExpense.getId());

        return convertToResponse(updatedExpense);
    }

    /**
     * Xóa expense.
     */
    @Override
    @Transactional
    public void deleteExpenseById(Long id) {
        log.debug("Deleting expense with id: {}", id);

        if (!expenseRepository.existsById(id)) {
            log.warn("Expense not found with id: {} for deletion", id);

            throw new ResourceNotFoundException("Expense với ID " + id + " không tồn tại");
        }

        expenseRepository.deleteById(id);

        log.info("Expense deleted successfully with id: {}", id);
    }

    /**
     * Convert Expense Entity -> ExpenseResponse DTO.
     */
    private ExpenseResponse convertToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .paidBy(expense.getPaidBy().getId())
                .build();
    }
}
