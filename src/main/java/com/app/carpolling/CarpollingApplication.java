package com.app.carpolling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarpollingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarpollingApplication.class, args);
	}

}
