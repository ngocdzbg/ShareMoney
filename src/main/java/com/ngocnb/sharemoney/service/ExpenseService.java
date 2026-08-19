package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import org.springframework.data.domain.Page;

public interface ExpenseService {
    Page<ExpenseResponse> getAllExpenses(
            int page,
            int size,
            String sortBy,
            String direction
    );

    ExpenseResponse getExpenseById(Long id);

    ExpenseResponse createExpense(ExpenseCreateRequest request);

    ExpenseResponse updateExpense(
            Long id,
            ExpenseUpdateRequest request
    );

    void deleteExpenseById(Long id);
}
