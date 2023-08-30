package com.twnch.eachbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan(basePackages = "com.twnch.eachbatch")
@SpringBootApplication(scanBasePackages = "com.twnch.eachbatch")
public class EachbatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(EachbatchApplication.class, args);
	}

}
