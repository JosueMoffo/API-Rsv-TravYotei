package com.example.Rsv_TravYotei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
public class RsvTravYoteiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RsvTravYoteiApplication.class, args);
		System.out.println("=================== START APP ===================");
	}

}
