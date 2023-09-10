package com.mottakin.onlineBookLibraryApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlineBookLibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineBookLibraryApplication.class, args);
	}
	@Bean
	public SpringApplicationContext springApplicationContext(){
		return new SpringApplicationContext();
	}

}
