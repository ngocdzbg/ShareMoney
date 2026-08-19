package com.ngocnb.sharemoney.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * User Entity - Lưu trữ thông tin user
 * <p>
 * ⭐ Thêm field roles để lưu các role của user
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    private String fullName;

    // ⭐ QUAN TRỌNG: Thêm field roles
    // @ElementCollection lưu collection trong table riêng
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",  // Tên table lưu roles
            joinColumns = @JoinColumn(name = "user_id")  // FK trỏ về user
    )
    @Column(name = "role")  // Tên column lưu role
    private List<String> roles = Arrays.asList("ROLE_MEMBER");  // Default role

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;
}
