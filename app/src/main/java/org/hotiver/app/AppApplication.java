package org.hotiver.app;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@EnableCaching
@SpringBootApplication(scanBasePackages = "org.hotiver")
public class AppApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.load();

			System.setProperty("DB_URL", dotenv.get("DB_URL"));
			System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
			System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
			System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
			System.setProperty("JWT_REFRESH_EXPIRATION", dotenv.get("JWT_REFRESH_EXPIRATION"));
			System.setProperty("JWT_ACCESS_EXPIRATION", dotenv.get("JWT_ACCESS_EXPIRATION"));
			System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));;
			System.setProperty("SERVICE_EMAIL", dotenv.get("SERVICE_EMAIL"));
			System.setProperty("EMAIL_PASSWORD", dotenv.get("EMAIL_PASSWORD"));
			System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
			System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}

		SpringApplication.run(AppApplication.class, args);
	}

}
