package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.response.PaymentResponse;
import com.ngocnb.sharemoney.dto.response.SettlementResponse;

import java.util.List;

public interface SettlementService {
    // Xem trước kết quả quyết toán (Không lưu DB)
    SettlementResponse calculateSettlement(Long groupId);

    // CHỐT SỔ: Tính toán và lưu các giao dịch thanh toán nợ vào DB
    List<PaymentResponse> finalizeSettlement(Long groupId, String note);
}
