package org.hotiver.service.redis;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OutboxProcessorService {

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String STREAM_KEY = "outbox:purchases";
    private static final String GROUP = "outbox-group";
    private static final String CONSUMER = "worker-1";

    public OutboxProcessorService(StringRedisTemplate redisTemplate,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void initGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP);
            System.out.println("Consumer group created");
        } catch (Exception e) {
            System.out.println("Consumer group already exists");
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void process() {

        var messages = redisTemplate.opsForStream()
                .read(
                        Consumer.from(GROUP, CONSUMER),
                        StreamReadOptions.empty().count(20),
                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                );

        assert messages != null;
        if (messages.isEmpty()) return;

        for (var msg : messages) {
            try {
                String payload = (String) msg.getValue().get("payload");
                kafkaTemplate.send("order-created-topic", payload).get();

                redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP, msg.getId());

            } catch (Exception e) {
                System.err.println("Retry later: " + e.getMessage());
            }
        }
    }

}
