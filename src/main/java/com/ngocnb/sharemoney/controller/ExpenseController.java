package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import com.ngocnb.sharemoney.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Lấy danh sách expense
     *
     * GET /api/expenses
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {

        log.info(
                "GET /api/expenses - page: {}, size: {}, sortBy: {}, direction: {}",
                page,
                size,
                sortBy,
                direction
        );

        Page<ExpenseResponse> expenses =
                expenseService.getAllExpenses(
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(expenses);
    }

    /**
     * Lấy expense theo ID
     *
     * GET /api/expenses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long id
    ) {

        log.info("GET /api/expenses/{}", id);

        ExpenseResponse expense =
                expenseService.getExpenseById(id);

        return ResponseEntity.ok(expense);
    }

    /**
     * Tạo expense mới
     *
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseCreateRequest request
    ) {

        log.info(
                "POST /api/expenses - description: {}, amount: {}",
                request.getDescription(),
                request.getAmount()
        );

        ExpenseResponse expense =
                expenseService.createExpense(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expense);
    }

    /**
     * Cập nhật một phần expense
     *
     * PATCH /api/expenses/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {

        log.info("PATCH /api/expenses/{}", id);

        ExpenseResponse expense =
                expenseService.updateExpense(id, request);

        return ResponseEntity.ok(expense);
    }

    /**
     * Xóa expense
     *
     * DELETE /api/expenses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id
    ) {

        log.info("DELETE /api/expenses/{}", id);

        expenseService.deleteExpenseById(id);

        return ResponseEntity.noContent().build();
    }
}