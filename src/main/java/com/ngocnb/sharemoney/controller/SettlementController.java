package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.dto.response.PaymentResponse;
import com.ngocnb.sharemoney.dto.response.SettlementResponse;
import com.ngocnb.sharemoney.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/settlement")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Settlement Management", description = "API quyết toán và bù trừ nợ tự động trong nhóm")
@SecurityRequirement(name = "Bearer Authentication")
public class SettlementController {
    private final SettlementService settlementService;

    @Operation(
            summary = "Tổng hợp quyết toán nợ trong nhóm",
            description = "Tính toán tổng số tiền đã chi tiêu của nhóm, số dư thu/chi của từng thành viên và tự động đề xuất danh sách các giao dịch chuyển tiền tối ưu nhất để bù trừ sạch nợ."
    )
    @GetMapping
    public ResponseEntity<SettlementResponse> getGroupSettlement(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId
    ) {
        log.info("GET /api/groups/{}/settlement - Calculating settlement", groupId);
        SettlementResponse response = settlementService.calculateSettlement(groupId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Chốt sổ và lưu lịch sử thanh toán nợ vào DB",
            description = "Tính toán quyết toán và chính thức tạo các bản ghi Payment lưu vào Database với trạng thái PENDING."
    )
    @PostMapping("/finalize")
    public ResponseEntity<List<PaymentResponse>> finalizeSettlement(
            @PathVariable Long groupId,
            @RequestParam(required = false) String note
    ) {
        log.info("POST /api/groups/{}/settlement/finalize - Finalizing settlement", groupId);
        List<PaymentResponse> payments = settlementService.finalizeSettlement(groupId, note);
        return ResponseEntity.ok(payments);
    }
}
