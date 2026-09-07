package com.ngocnb.sharemoney.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;

    public void deleteExpenseCacheByGroupId(Long groupId) {

        String pattern = "expenses:group:" + groupId + ":*";

        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        List<String> keys = new ArrayList<>();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {

            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }

        if (keys.isEmpty()) {
            log.info("Redis CACHE INVALIDATION: no keys found for group {}", groupId);
            return;
        }

        redisTemplate.delete(keys);

        log.info(
                "Redis CACHE INVALIDATION: deleted {} keys for group {}",
                keys.size(),
                groupId
        );
    }
}