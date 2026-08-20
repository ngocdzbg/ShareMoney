package com.ngocnb.sharemoney.dto.request;

import com.ngocnb.sharemoney.entity.GroupMember;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseCreateRequest {

    @NotNull(message = "Group ID không được để trống")
    private Long groupId;

    @NotBlank(message = "Description không được để trống")
    private String description;

    @NotNull(message = "Amount không được để trống")
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull(message = "Expense date không được để trống")
    private LocalDate expenseDate;

    @NotNull(message = "Paid by không được để trống")
    private GroupMember paidBy;

    @NotEmpty(message = "Phải có ít nhất một người tham gia")
    private List<Long> participantIds;
}