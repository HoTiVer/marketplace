package org.hotiver.app;


import org.hotiver.config.kafka.KafkaProducer;
import org.hotiver.config.redis.RedisStartupChecker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.TimeZone;

@SpringBootTest
class AppApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() { }
}
