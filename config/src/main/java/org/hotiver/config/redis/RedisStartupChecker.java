package org.hotiver.config.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisStartupChecker {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisStartupChecker(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @PostConstruct
    public void checkRedisConnection(){
        try {
            var connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();
            if (pong == null || !"PONG".equalsIgnoreCase(pong)) {
                throw new IllegalStateException("Redis is not responding correctly!");
            }
            log.info("Redis connected: " + pong);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot connect to Redis at startup!", e);
        }
    }

}
