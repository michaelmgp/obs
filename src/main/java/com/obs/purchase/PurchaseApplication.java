package com.obs.purchase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PurchaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(PurchaseApplication.class, args);
	}

}
