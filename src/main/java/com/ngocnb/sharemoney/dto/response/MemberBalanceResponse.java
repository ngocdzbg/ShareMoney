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
public class MemberBalanceResponse {
    private Long memberId;
    private String memberName;
    private BigDecimal totalPaid;     // Tổng tiền đã đứng ra trả hộ nhóm
    private BigDecimal totalExpense;  // Tổng tiền bản thân thực tế đã tiêu
    private BigDecimal netBalance;    // Cân đối (+ là được nhận lại, - là phải trả thêm)
}