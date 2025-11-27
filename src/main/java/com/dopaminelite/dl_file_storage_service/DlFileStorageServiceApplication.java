package com.dopaminelite.dl_file_storage_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DlFileStorageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DlFileStorageServiceApplication.class, args);
	}

}
