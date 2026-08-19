package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseUpdateRequest {

    private Long groupId;

    @Size(
            max = 255,
            message = "Description không được vượt quá 255 ký tự"
    )
    private String description;

    @DecimalMin(
            value = "0.01",
            message = "Amount phải lớn hơn 0"
    )
    private BigDecimal amount;

    private LocalDate expenseDate;

    private Long paidBy;
}