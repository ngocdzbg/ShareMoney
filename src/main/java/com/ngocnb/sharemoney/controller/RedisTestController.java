package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisCacheService redisCacheService;

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> set() {

        boolean saved = redisCacheService.setSafely(
                "test:key",
                "Hello Redis",
                Duration.ofMinutes(5)
        );

        return ResponseEntity.ok(Map.of(
                "redisAvailable", saved,
                "message", saved ? "Redis value saved" : "Redis unavailable, write skipped"
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> get() {

        return redisCacheService.getSafely("test:key")
                .<ResponseEntity<Map<String, Object>>>map(value -> ResponseEntity.ok(Map.of(
                        "redisAvailable", true,
                        "cacheHit", true,
                        "value", value
                )))
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "redisAvailable", false,
                        "cacheHit", false
                )));
    }
}
