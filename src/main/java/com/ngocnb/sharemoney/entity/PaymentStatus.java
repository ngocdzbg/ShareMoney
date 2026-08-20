package com.ngocnb.sharemoney.entity;

public enum PaymentStatus {
    PENDING,    // Chờ thành viên chuyển khoản / trả tiền
    COMPLETED,  // Đã xác nhận trả tiền xong
    CANCELLED   // Đã hủy
}
