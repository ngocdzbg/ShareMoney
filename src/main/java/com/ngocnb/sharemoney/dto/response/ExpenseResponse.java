package com.ngocnb.sharemoney.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ExpenseResponse {

    private Long id;

    private Long groupId;

    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;

    private Long paidBy;

    private List<Long> participantIds;
}