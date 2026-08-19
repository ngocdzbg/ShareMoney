package com.ngocnb.sharemoney.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LoginResponse - Trả về khi login thành công
 * <p>
 * ⭐ Thêm field roles để client biết user có quyền gì
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT token - gửi kèm mỗi request trong Authorization header
     */
    private String accessToken;

    private String refreshToken;

    /**
     * Username của user
     */
    private String username;

    /**
     * ⭐ Danh sách roles của user
     * Ví dụ: ["ROLE_ADMIN", "ROLE_MEMBER"]
     * <p>
     * Frontend dùng để biết user có quyền gì
     */
    private List<String> roles;
}