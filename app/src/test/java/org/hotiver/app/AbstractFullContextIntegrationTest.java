package org.hotiver.app;

import org.hotiver.config.kafka.KafkaProducer;
import org.hotiver.config.redis.RedisStartupChecker;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbstractFullContextIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private RedisStartupChecker redisStartupChecker;

    @MockitoBean
    private KafkaProducer kafkaProducer;
}
