package com.example.Rsv_TravYotei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableRetry
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.Rsv_TravYotei.repository")
public class RsvTravYoteiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RsvTravYoteiApplication.class, args);
		System.out.println("=================== START APP ===================");
	}
}
