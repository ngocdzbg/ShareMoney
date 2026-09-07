package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ExpenseUpdateRequest {

    @NotBlank(message = "Mô tả khoản chi không được để trống")
    private String description;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull
    private Long version;

    @NotNull(message = "Ngày chi tiêu không được để trống")
    private LocalDate expenseDate;

    @NotNull(message = "ID người trả tiền không được để trống")
    private Long paidById;

    @NotEmpty(message = "Danh sách người tham gia chia tiền không được để trống")
    private List<Long> participantIds;
}