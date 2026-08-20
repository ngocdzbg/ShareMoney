package com.ngocnb.sharemoney.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementResponse {
    private Long groupId;
    private BigDecimal totalGroupExpense;            // Tổng chi tiêu toàn nhóm
    private List<MemberBalanceResponse> memberBalances; // Chi tiết thu/chi từng người
    private List<DebtTransferResponse> transfers;      // Các bước chuyển tiền tối ưu nhất
}