package com.ogidazepam.job_api_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class JobApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobApiServiceApplication.class, args);
	}

}
