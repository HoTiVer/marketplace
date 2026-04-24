package org.hotiver.app;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication(scanBasePackages = "org.hotiver")
public class AppApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.load();

			System.setProperty("SPRING_PROFILES_ACTIVE", dotenv.get("SPRING_PROFILES_ACTIVE"));
			System.setProperty("DB_URL", dotenv.get("DB_URL"));
			System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
			System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
			System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
			System.setProperty("JWT_REFRESH_EXPIRATION", dotenv.get("JWT_REFRESH_EXPIRATION"));
			System.setProperty("JWT_ACCESS_EXPIRATION", dotenv.get("JWT_ACCESS_EXPIRATION"));
			System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));
			System.setProperty("SERVICE_EMAIL", dotenv.get("SERVICE_EMAIL"));
			System.setProperty("EMAIL_PASSWORD", dotenv.get("EMAIL_PASSWORD"));
			System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
			System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
            System.setProperty("KAFKA_BOOTSTRAP_SERVERS", dotenv.get("KAFKA_BOOTSTRAP_SERVERS"));
			System.setProperty("FRONTEND_URL", dotenv.get("FRONTEND_URL"));
			System.setProperty("IMAGE_STORAGE_HOST", dotenv.get("IMAGE_STORAGE_HOST"));
			System.setProperty("MINIO_USER", dotenv.get("MINIO_USER"));
			System.setProperty("MINIO_PASSWORD", dotenv.get("MINIO_PASSWORD"));
			System.setProperty("MINIO_BUCKET", dotenv.get("MINIO_BUCKET"));
			System.setProperty("MAX_PRODUCT_IMAGES_COUNT", dotenv.get("MAX_PRODUCT_IMAGES_COUNT"));
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}

		SpringApplication.run(AppApplication.class, args);
	}

}
