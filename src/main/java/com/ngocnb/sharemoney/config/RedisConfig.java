package com.ngocnb.sharemoney.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisConfig {

    @Bean
    public ApplicationRunner redisConnectionInfo(
            RedisConnectionFactory redisConnectionFactory
    ) {
        return args -> {
            System.out.println("=================================");
            System.out.println("REDIS FACTORY");
            System.out.println(redisConnectionFactory.getClass().getName());

            var connection = redisConnectionFactory.getConnection();

            try {
                System.out.println("CONNECTION");
                System.out.println(connection);
                System.out.println("PING: " + connection.ping());
                System.out.println("DB SIZE: " + connection.serverCommands().dbSize());
            } finally {
                connection.close();
            }

            System.out.println("=================================");
        };
    }
}