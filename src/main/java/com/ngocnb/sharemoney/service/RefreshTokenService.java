package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.entity.RefreshToken;
import com.ngocnb.sharemoney.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);

    RefreshToken findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);
}
