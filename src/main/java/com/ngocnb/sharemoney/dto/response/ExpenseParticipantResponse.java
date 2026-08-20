package com.ngocnb.sharemoney.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseParticipantResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private BigDecimal shareAmount; // Số tiền cần trả/gánh cho khoản này
}