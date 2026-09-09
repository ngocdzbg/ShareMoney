package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.observability.ExceptionLogUtils;
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
            logRedisFailure("SCAN", pattern, exception);
            throw exception;
        }

        if (keys.isEmpty()) {
            log.info("Redis CACHE INVALIDATION: no keys found for group {}", groupId);
            return;
        }

        try {
            redisTemplate.delete(keys);
        } catch (Exception exception) {
            logRedisFailure("DELETE", pattern, exception);
            throw exception;
        }

        log.info(
                "Redis CACHE INVALIDATION: deleted {} keys for group {}",
                keys.size(),
                groupId
        );
    }

    private void logRedisFailure(String operation, String keyOrPattern, Exception exception) {
        log.error(
                "Redis operation failed operation={} keyOrPattern={} exception={}",
                operation,
                keyOrPattern,
                ExceptionLogUtils.describe(exception)
        );

        Throwable rootCause = ExceptionLogUtils.rootCause(exception);
        if (rootCause != null && rootCause != exception) {
            log.error("Redis root cause: {}", ExceptionLogUtils.describe(rootCause));
        }
    }
}
