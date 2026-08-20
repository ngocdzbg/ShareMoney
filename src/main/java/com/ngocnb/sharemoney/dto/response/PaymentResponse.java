package com.ngocnb.sharemoney.dto.response;

import com.ngocnb.sharemoney.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long groupId;
    private Long fromMemberId;
    private String fromMemberName;
    private Long toMemberId;
    private String toMemberName;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private String note;
}