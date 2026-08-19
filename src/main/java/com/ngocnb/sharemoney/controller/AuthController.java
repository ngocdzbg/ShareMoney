package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.dto.request.LoginRequest;
import com.ngocnb.sharemoney.dto.request.RefreshTokenRequest;
import com.ngocnb.sharemoney.dto.request.RegisterRequest;
import com.ngocnb.sharemoney.dto.response.LoginResponse;
import com.ngocnb.sharemoney.entity.RefreshToken;
import com.ngocnb.sharemoney.entity.User;
import com.ngocnb.sharemoney.exception.ResourceNotFoundException;
import com.ngocnb.sharemoney.security.JwtService;
import com.ngocnb.sharemoney.service.RefreshTokenService;
import com.ngocnb.sharemoney.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Auth Controller - Đăng nhập
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        userService.saveUser(request);
        return ResponseEntity.ok("Đăng ký thành công");
    }

    /**
     * Đăng nhập - Trả về JWT token kèm roles
     * <p>
     * ⭐ QUAN TRỌNG: Khi login, ta lấy roles của user từ database
     * và thêm vào JWT token
     *
     * @param request Chứa username và password
     * @return Token và thông tin user
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1️⃣ Tìm user trong database
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2️⃣ Kiểm tra password (trong thực tế phải dùng password encoder)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // ⭐ 3️⃣ LẤY ROLES CỦA USER
        // Giả sử User entity có field List<String> roles
        // Ví dụ: user.getRoles() = ["ROLE_ADMIN", "ROLE_MEMBER"]
        java.util.List<String> roles = user.getRoles();

        // ⭐ 4️⃣ TẠO JWT TOKEN VỚI ROLES
        // Token sẽ chứa: { sub: "admin", roles: ["ROLE_ADMIN"] }
        String token = jwtService.generateToken(user.getUsername(), roles);
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);
        String refreshToken = refreshTokenEntity.getToken();
        // 5️⃣ TRẢ VỀ RESPONSE
        LoginResponse response = LoginResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .roles(roles)  // ⭐ Gửi roles về cho client để client biết user có quyền gì
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> loginByRefreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        // Cấp accessToken mới
        String newAccessToken = jwtService.generateToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken()) // refresh token cũ vẫn còn hạn nên giữ nguyên
                .username(user.getUsername())
                .roles(user.getRoles())
                .build());
    }
}