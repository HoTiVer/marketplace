package org.hotiver.app;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.hotiver")
public class AppApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.load();

			System.setProperty("DB_URL", dotenv.get("DB_URL"));
			System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
			System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
			System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
			System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));
			System.setProperty("SERVICE_EMAIL", dotenv.get("SERVICE_EMAIL"));
			System.setProperty("EMAIL_PASSWORD", dotenv.get("EMAIL_PASSWORD"));
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}


		SpringApplication.run(AppApplication.class, args);
	}

}
