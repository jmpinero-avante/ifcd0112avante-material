package com.avante.springbootjma01.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.avante.springbootjma01.model.AuthService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig {
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner seedUser(AuthService auth) {
		return args -> auth.createIfNotExists(
			"demo@empresa.com",
			"12345",
			"Usuario Demo"
		);
	}
}
