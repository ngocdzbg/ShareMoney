package com.ngocnb.sharemoney.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private Long groupId;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;

    // Thông tin người trả tiền
    private Long paidByMemberId;
    private String paidByMemberName;

    // Danh sách chia tiền cho từng người
    private List<ExpenseParticipantResponse> participants;
}