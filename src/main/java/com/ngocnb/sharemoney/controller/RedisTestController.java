package com.ngocnb.sharemoney.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    @PostMapping("/test")
    public String set() {

        redisTemplate.opsForValue().set(
                "test:key",
                "Hello Redis",
                Duration.ofMinutes(5)
        );

        return "Redis value saved";
    }

    @GetMapping("/test")
    public String get() {

        return redisTemplate
                .opsForValue()
                .get("test:key");
    }
}