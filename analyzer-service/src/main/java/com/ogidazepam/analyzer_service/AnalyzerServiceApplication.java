package com.ogidazepam.analyzer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class AnalyzerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyzerServiceApplication.class, args);
	}

}
