package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.request.RegisterRequest;
import com.ngocnb.sharemoney.entity.User;
import com.ngocnb.sharemoney.repository.UserRepository;
import com.ngocnb.sharemoney.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User saveUser(RegisterRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.findUserByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // ⭐ mã hoá password
                .email(request.getEmail())
                .fullName(request.getFullName())
                .roles(List.of("ROLE_MEMBER")) // role mặc định khi tự đăng ký
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        return userRepository.save(user);
    }
}