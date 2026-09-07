package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.dto.request.ExpenseCreateRequest;
import com.ngocnb.sharemoney.dto.request.ExpenseUpdateRequest;
import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import com.ngocnb.sharemoney.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Expense Management", description = "API quản lý khoản chi và chia tiền trong nhóm")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
            summary = "Tạo khoản chi mới và tự động chia đều",
            description = "Tạo khoản chi tiêu mới trong nhóm, ghi nhận người ứng tiền trước (paidById) và tự động tính toán chia đều khoản tiền đó cho danh sách người tham gia (participantIds), hỗ trợ làm tròn số tiền lẻ chuẩn xác."
    )
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @Valid @RequestBody ExpenseCreateRequest request
    ) {
        log.info("POST /api/groups/{}/expenses - Create expense: {}", groupId, request.getDescription());
        ExpenseResponse response = expenseService.createExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Lấy thông tin chi tiết khoản chi",
            description = "Trả về thông tin chi tiết một khoản chi bao gồm: mô tả, ngày chi, số tiền, người trả tiền và chi tiết số tiền phân bổ cho từng người tham gia."
    )
    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @PathVariable
            @Positive(message = "ExpenseId must be greater than 0")
            Long expenseId
    ) {
        log.info("GET /api/groups/{}/expenses/{}", groupId, expenseId);
        ExpenseResponse response = expenseService.getExpenseById(groupId, expenseId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Lấy danh sách khoản chi của nhóm (Phân trang)",
            description = "Lấy danh sách phân trang các khoản chi thuộc về nhóm chỉ định, hỗ trợ sắp xếp theo ID, số tiền hoặc ngày chi tiêu."
    )
    @GetMapping
    public ResponseEntity<Slice<ExpenseResponse>> getAllExpenses(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,

            @Parameter(description = "Số trang (zero-based, mặc định 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng items per trang (max 30)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Trường sắp xếp: id, amount, expenseDate")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Hướng sắp xếp: ASC hoặc DESC")
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        log.info("GET /api/groups/{}/expenses - page: {}, size: {}, sortBy: {}, direction: {}",
                groupId, page, size, sortBy, direction);
        Slice<ExpenseResponse> expenses = expenseService.getExpensesByGroupId(groupId, page, size, sortBy, direction);
        return ResponseEntity.ok(expenses);
    }

    @Operation(
            summary = "Cập nhật khoản chi",
            description = "Cập nhật nội dung, số tiền, người trả hoặc danh sách người tham gia khoản chi. Hệ thống sẽ tính toán và chia lại số tiền cho từng thành viên."
    )
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @PathVariable
            @Positive(message = "ExpenseId must be greater than 0")
            Long expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        log.info("PUT /api/groups/{}/expenses/{} - Update expense", groupId, expenseId);
        ExpenseResponse response = expenseService.updateExpense(groupId, expenseId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Xóa khoản chi",
            description = "Xóa bỏ một khoản chi tiêu và toàn bộ danh sách phân chia tiền tương ứng (ExpenseParticipants) khỏi hệ thống."
    )
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @PathVariable
            @Positive(message = "ExpenseId must be greater than 0")
            Long expenseId
    ) {
        log.info("DELETE /api/groups/{}/expenses/{} - Delete expense", groupId, expenseId);
        expenseService.deleteExpense(groupId, expenseId);
        return ResponseEntity.noContent().build();
    }
}