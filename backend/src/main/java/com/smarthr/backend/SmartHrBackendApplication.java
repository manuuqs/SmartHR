package com.smarthr.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan("com.smarthr.backend.domain")
public class SmartHrBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHrBackendApplication.class, args);
	}

}
