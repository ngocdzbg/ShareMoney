package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.RegisterRequest;
import com.ngocnb.sharemoney.entity.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);

    User saveUser(RegisterRequest u);
}
