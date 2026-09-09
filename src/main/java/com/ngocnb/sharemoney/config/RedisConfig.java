package com.ngocnb.sharemoney.config;

import com.ngocnb.sharemoney.observability.ExceptionLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@ConditionalOnProperty(
        name = "app.redis.enabled",
        havingValue = "true"
)
@Slf4j
public class RedisConfig {

    @Bean
    public ApplicationRunner redisConnectionInfo(
            RedisConnectionFactory redisConnectionFactory
    ) {
        return args -> {
            try {
                log.info(
                        "Redis startup check started connectionFactory={}",
                        redisConnectionFactory.getClass().getName()
                );

                var connection = redisConnectionFactory.getConnection();

                try {
                    log.info("Redis startup check ping={}", connection.ping());
                    log.info("Redis startup check dbSize={}", connection.serverCommands().dbSize());
                } finally {
                    connection.close();
                }
            } catch (Exception exception) {
                log.error(
                        "Redis startup check failed: {}",
                        ExceptionLogUtils.describe(exception)
                );

                Throwable rootCause = ExceptionLogUtils.rootCause(exception);
                if (rootCause != null && rootCause != exception) {
                    log.error("Redis startup root cause: {}", ExceptionLogUtils.describe(rootCause));
                }

                throw exception;
            }
        };
    }
}
