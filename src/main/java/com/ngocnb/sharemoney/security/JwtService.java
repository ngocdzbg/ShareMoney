package com.ngocnb.sharemoney.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service - JWT Token Management
 * <p>
 * Chức năng:
 * - Generate JWT token từ username
 * - Validate JWT token
 * - Extract username từ token
 * <p>
 * JWT Structure:
 * - Header: {alg: "HS256", typ: "JWT"}
 * - Payload: {sub: "username", iat: timestamp, exp: timestamp}
 * - Signature: HMAC-SHA256(header + payload, secret)
 *
 * @author NgocNB
 * @version 2026.0217
 * @since 2026
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Secret key để sign JWT tokens
     * Được load từ application.properties (jwt.secret)
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Token expiration time (milliseconds)
     * Được load từ application.properties (jwt.expiration)
     * Mặc định: 86400000 ms = 24 giờ
     */
    @Value("${jwt.expiration}")
    private long expirationMs;

    @Value("${jwt.access-token-expiration}") // ms, ví dụ 900000 = 15 phút
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}") // ms, ví dụ 604800000 = 7 ngày
    private long refreshTokenExpiration;


    /**
     * Generate JWT token từ username
     * <p>
     * Flow:
     * 1. Tạo secret key từ jwtSecret string
     * 2. Tính thời gian hết hạn
     * 3. Build JWT token với claims
     * 4. Sign bằng secret key
     * 5. Compact to string và return
     *
     * @param username Username đưa vào token
     * @return JWT token (String)
     */
    public String generateToken(String username, List<String> roles) {
        log.debug("Generating JWT token for username: {}", username);

        try {
            Map<String, Object> claims = new HashMap<>();

            // ⭐ Thêm roles vào JWT token
            claims.put("roles", roles);
            String token = Jwts.builder()
                    .subject(username) // Lưu tên người dùng vào Payload
                    .claim("role", roles) // Thêm thông tin vai trò vào Payload
                    .issuedAt(new Date()) // Ngày tạo
//                    .expiration(new Date(System.currentTimeMillis() + expirationMs)) // Ngày hết hạn
                    .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) // Ngày hết hạn
                    .signWith(getSigningKey()) // Ký tên bằng chìa khóa bí mật
                    .compact();

            log.info("JWT token generated successfully for user: {}", username);
            return token;

        } catch (Exception e) {
            log.error("Error generating JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    // Refresh token chỉ là chuỗi random, lưu và kiểm tra hạn ở DB (không cần decode JWT)
    public String generateRefreshToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("role", roles)
                .claim("type", "refresh") // đánh dấu đây là refresh token, tránh nhầm với access token
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpiration;
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validate JWT token
     * <p>
     * Kiểm tra:
     * - Token không bị tampered (signature hợp lệ)
     * - Token không hết hạn
     * - Token format đúng
     *
     * @param token JWT token cần validate
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean isValidToken(String token) {
        try {
            getClaimsFromToken(token);
            log.debug("Token validation successful");
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT token is invalid: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username từ JWT token
     *
     * @param token JWT token
     * @return Username được extract, hoặc null nếu không thể extract
     */
    public String extractUsername(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            log.debug("Username extracted from token: {}", username);
            return username;

        } catch (ExpiredJwtException e) {
            log.warn("Cannot extract username - token expired");
            return null;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Cannot extract username - invalid token");
            return null;

        } catch (Exception e) {
            log.error("Error extracting username: {}", e.getMessage());
            return null;
        }
    }

    // ============ ⭐ EXTRACT ROLES ============

    /**
     * Lấy danh sách roles từ JWT token
     * <p>
     * ⭐ METHOD MỚI - DÙNG CHO PHÂN QUYỀN
     *
     * @param token JWT token
     * @return List of role strings (ví dụ: ["ROLE_ADMIN", "ROLE_MEMBER"])
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }

        return Collections.emptyList();  // Return empty list nếu không có roles
    }

    // ============ ⭐ EXTRACT AUTHORITIES ============

    /**
     * Lấy danh sách GrantedAuthority từ JWT token
     * <p>
     * ⭐ METHOD MỚI - DÙNG CHO SPRING SECURITY AUTHORIZATION
     *
     * @param token JWT token
     * @return List of GrantedAuthority
     * (ví dụ: [SimpleGrantedAuthority("ROLE_ADMIN"), SimpleGrantedAuthority("ROLE_MEMBER")])
     */
    public List<GrantedAuthority> extractAuthorities(String token) {
        List<String> roles = extractRoles(token);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)  // Chuyển "ROLE_ADMIN" → SimpleGrantedAuthority("ROLE_ADMIN")
                .collect(Collectors.toList());
    }

    // ============ HELPER METHODS ============

    /**
     * Lấy tất cả claims từ JWT token
     *
     * @param token JWT token
     * @return Claims object
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Hàm hỗ trợ: Tạo SecretKey từ secretKey string
     *
     * @return SecretKey được tạo từ secretKey string dùng để sign và verify JWT tokens. SecretKey phải đủ dài (ít nhất 256 bit - 32 kí tự) để đảm bảo an toàn khi sử dụng HMAC-SHA256
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Hàm hỗ trợ: Lấy claims từ token
     *
     * @param token JWT token từ client gửi lên
     * @return Claims chứa thông tin payload của token
     * @throws JwtException nếu token không hợp lệ hoặc đã hết hạn
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
