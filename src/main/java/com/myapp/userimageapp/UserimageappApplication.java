package com.myapp.userimageapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class UserimageappApplication {

	public static void main(String[] args) {
		log.info("Starting application!");
		SpringApplication.run(UserimageappApplication.class, args);
	}

}
