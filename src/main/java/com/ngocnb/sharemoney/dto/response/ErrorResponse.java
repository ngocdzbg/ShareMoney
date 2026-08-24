package com.ngocnb.sharemoney.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO - Error Response
 * <p>
 * Cấu trúc thống nhất cho tất cả error responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Thời gian error xảy ra
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error type (NOT_FOUND, VALIDATION_ERROR, v.v.)
     */
    private String error;

    /**
     * Error message
     */
    private String message;

    /**
     * Chi tiết lỗi (thường là validation errors)
     */
    private String details;

    /**
     * Request path
     */
    private String path;
}