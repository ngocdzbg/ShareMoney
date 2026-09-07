package com.ngocnb.sharemoney.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseEvent {

    private String eventId;

    private ExpenseEventType eventType;

    private Long expenseId;

    private Long groupId;

    private Long paidByMemberId;

    private BigDecimal amount;

    private String description;

    private LocalDateTime occurredAt;
}