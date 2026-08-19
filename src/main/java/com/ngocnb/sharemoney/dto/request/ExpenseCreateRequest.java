package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseCreateRequest {

    @NotNull(message = "Group ID không được để trống")
    private Long groupId;

    @Size(max = 255, message = "Description không được vượt quá 255 ký tự")
    private String description;

    @NotNull(message = "Amount không được để trống")
    @DecimalMin(value = "0.01", message = "Amount phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Expense date không được để trống")
    private LocalDate expenseDate;

    @NotNull(message = "Paid by không được để trống")
    private Long paidBy;
}