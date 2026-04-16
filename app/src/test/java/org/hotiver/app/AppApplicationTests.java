package org.hotiver.app;

import io.minio.MinioClient;
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
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.TimeZone;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class AppApplicationTests {

	static {
		System.setProperty("user.timezone", "UTC");
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@MockitoBean
	private RedisStartupChecker redisStartupChecker;

	@MockitoBean
	private KafkaProducer kafkaProducer;

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");

	@Container
	static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
			.withUserName("testuser")
			.withPassword("testpassword");

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		registry.add("minio.endpoint", minio::getS3URL);
		registry.add("minio.user", minio::getUserName);
		registry.add("minio.password", minio::getPassword);
		registry.add("minio.bucket", () -> "test-bucket");

		String minioUrl = minio.getS3URL();
		registry.add("minio.endpoint", () -> minioUrl);
		registry.add("storage.host", () -> minioUrl);

		registry.add("storage.max-images-count.product", () -> 10);

		registry.add("frontend.url", () -> "frontend");
	}

	@Test
	void contextLoads() { }
}
