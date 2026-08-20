package com.ngocnb.sharemoney.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DebtTransferResponse {
    private Long fromMemberId;
    private String fromMemberName;  // Người trả tiền
    private Long toMemberId;
    private String toMemberName;    // Người nhận tiền
    private BigDecimal amount;       // Số tiền cần chuyển
}