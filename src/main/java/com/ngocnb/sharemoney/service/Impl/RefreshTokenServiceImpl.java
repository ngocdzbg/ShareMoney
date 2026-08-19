package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.entity.RefreshToken;
import com.ngocnb.sharemoney.entity.User;
import com.ngocnb.sharemoney.repository.RefreshTokenRepository;
import com.ngocnb.sharemoney.security.JwtService;
import com.ngocnb.sharemoney.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtService.generateRefreshToken(user.getUsername(), user.getRoles()))
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        // Check hạn ở DB (để đồng bộ, và để lỡ token bị revoke thủ công thì cũng bắt được)
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }
        // ⭐ Check thêm chữ ký JWT — phòng trường hợp secret bị đổi hoặc token bị chỉnh sửa
        if (!jwtService.isTokenValid(token.getToken())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token không hợp lệ");
        }
        return token;
    }
}
