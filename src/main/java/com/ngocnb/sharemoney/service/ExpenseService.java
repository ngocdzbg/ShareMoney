package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import org.springframework.data.domain.Page;

public interface ExpenseService {
    ExpenseResponse createExpense(Long groupId, ExpenseCreateRequest request);

    ExpenseResponse getExpenseById(Long groupId, Long expenseId);

    Page<ExpenseResponse> getExpensesByGroupId(Long groupId, int page, int size, String sortBy, String direction);

    ExpenseResponse updateExpense(Long groupId, Long expenseId, ExpenseUpdateRequest request);

    void deleteExpense(Long groupId, Long expenseId);
}
