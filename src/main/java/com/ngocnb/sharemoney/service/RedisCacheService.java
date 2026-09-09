package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.observability.ExceptionLogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private static final long DEFAULT_EXPENSE_GROUP_CACHE_VERSION = 0L;

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

    public Optional<Long> getExpenseGroupVersionSafely(Long groupId) {
        String key = buildExpenseGroupVersionKey(groupId);

        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null || value.isBlank()) {
                return Optional.of(DEFAULT_EXPENSE_GROUP_CACHE_VERSION);
            }

            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            log.warn(
                    "Invalid Redis cache version, fallback to database. operation=GET_VERSION groupId={} rootCause={}",
                    groupId,
                    rootCauseDescription(exception)
            );
            deleteSafely(key);
            return Optional.empty();
        } catch (Exception exception) {
            log.warn(
                    "Redis unavailable, fallback to database. operation=GET_VERSION groupId={} rootCause={}",
                    groupId,
                    rootCauseDescription(exception)
            );
            return Optional.empty();
        }
    }

    public boolean incrementExpenseGroupVersionSafely(Long groupId) {
        String key = buildExpenseGroupVersionKey(groupId);

        try {
            Long version = redisTemplate.opsForValue().increment(key);
            log.info(
                    "Redis cache version incremented operation=INCR groupId={} version={}",
                    groupId,
                    version
            );
            return true;
        } catch (Exception exception) {
            log.warn(
                    "Redis unavailable, skip cache invalidation. operation=INCR_VERSION groupId={} rootCause={}",
                    groupId,
                    rootCauseDescription(exception)
            );
            return false;
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

    public void invalidateExpenseCacheByGroupId(Long groupId) {
        incrementExpenseGroupVersionSafely(groupId);
    }

    public void deleteExpenseCacheByGroupId(Long groupId) {
        invalidateExpenseCacheByGroupId(groupId);
    }

    private String rootCauseDescription(Exception exception) {
        Throwable rootCause = ExceptionLogUtils.rootCause(exception);
        return ExceptionLogUtils.describe(rootCause != null ? rootCause : exception);
    }

    private String buildExpenseGroupVersionKey(Long groupId) {
        return "expenses:group:" + groupId + ":version";
    }
}
