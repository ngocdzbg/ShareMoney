package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "refreshToken không được để trống")
    private String refreshToken;
}
