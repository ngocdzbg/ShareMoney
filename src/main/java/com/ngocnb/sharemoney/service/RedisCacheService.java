package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.observability.ExceptionLogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;

    public Optional<String> getSafely(String key) {
        try {
            log.info("Redis cache read started operation=GET key={}", key);
            String value = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(value);
        } catch (Exception exception) {
            log.warn(
                    "Redis unavailable, fallback to database. operation=GET key={} rootCause={}",
                    key,
                    rootCauseDescription(exception)
            );
            return Optional.empty();
        }
    }

    public boolean setSafely(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.info("Redis cache write completed operation=SET key={} ttl={}", key, ttl);
            return true;
        } catch (Exception exception) {
            log.warn(
                    "Redis unavailable, skip cache write. operation=SET key={} rootCause={}",
                    key,
                    rootCauseDescription(exception)
            );
            return false;
        }
    }

    public boolean deleteSafely(String key) {
        try {
            redisTemplate.delete(key);
            log.info("Redis cache delete completed operation=DELETE key={}", key);
            return true;
        } catch (Exception exception) {
            log.warn(
                    "Redis unavailable, skip cache delete. operation=DELETE key={} rootCause={}",
                    key,
                    rootCauseDescription(exception)
            );
            return false;
        }
    }

    public void deleteExpenseCacheByGroupId(Long groupId) {

        String pattern = "expenses:group:" + groupId + ":*";
        log.info(
                "Service RedisCacheService.deleteExpenseCacheByGroupId started groupId={} operation=SCAN pattern={}",
                groupId,
                pattern
        );

        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        List<String> keys = new ArrayList<>();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {

            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception exception) {
            log.warn(
                    "Redis unavailable, skip cache invalidation. operation=SCAN groupId={} rootCause={}",
                    groupId,
                    rootCauseDescription(exception)
            );
            return;
        }

        if (keys.isEmpty()) {
            log.info("Redis CACHE INVALIDATION: no keys found for group {}", groupId);
            return;
        }

        try {
            redisTemplate.delete(keys);
        } catch (Exception exception) {
            log.warn(
                    "Redis unavailable, skip cache invalidation. operation=DELETE groupId={} keyCount={} rootCause={}",
                    groupId,
                    keys.size(),
                    rootCauseDescription(exception)
            );
            return;
        }

        log.info(
                "Redis CACHE INVALIDATION: deleted {} keys for group {}",
                keys.size(),
                groupId
        );
    }

    private String rootCauseDescription(Exception exception) {
        Throwable rootCause = ExceptionLogUtils.rootCause(exception);
        return ExceptionLogUtils.describe(rootCause != null ? rootCause : exception);
    }
}
