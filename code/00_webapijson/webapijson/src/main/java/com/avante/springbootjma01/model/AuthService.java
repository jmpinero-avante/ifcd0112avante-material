package com.avante.springbootjma01.model;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository repo;
	private final BCryptPasswordEncoder encoder;

	public Optional<User> authenticate (String email, String rawPassword) {
		return
			repo.findByEmail(email)
				.filter(
					u -> encoder.matches(rawPassword,u.getPasswordHash())
				);
	}

	public void createIfNotExists(String email, String password, String fullName) {
		if (repo.findByEmail(email).isEmpty()) {
			repo.save(
				User.builder()
					.email(email)
					.passwordHash(encoder.encode(password))
					.fullName(fullName)
					.build()
			);
		}
	}
}