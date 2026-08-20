package com.ngocnb.sharemoney.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     *
     * @param message Mô tả lỗi
     * @param cause   Nguyên nhân gốc
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
