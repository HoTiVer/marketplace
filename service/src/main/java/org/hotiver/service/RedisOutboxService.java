package org.hotiver.service;

import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RedisOutboxService {

    private final StringRedisTemplate redisTemplate;
    private static final String STREAM_KEY = "outbox:purchases";
    private static final int MAX_STREAM_LENGTH = 100_000;


    public RedisOutboxService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void savePurchaseEvent(String orderJson) {
        redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                        .in(STREAM_KEY)
                        .ofMap(Map.of(
                                "eventType", "ORDER_CREATED",
                                "payload", orderJson
                        ))
        );

        redisTemplate.opsForStream().trim(STREAM_KEY, MAX_STREAM_LENGTH, true);
    }

}
