package com.avante.springbootjma01.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avante.springbootjma01.model.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService auth;

	public record LoginRequest(
		@Email
		String email,

		@NotBlank
		String password
	) {}

	@PostMapping("/login")
	public ResponseEntity<?> login (
		@RequestBody
		LoginRequest req,
		
		HttpSession session
	){
		return
			this.auth.authenticate(req.email(), req.password())
				.<ResponseEntity<?>>map(
					u -> {
						session.setAttribute("userId", u.getId());
						session.setAttribute("fullName", u.getFullName());

						return ResponseEntity.ok(
							Map.of(
								"id", u.getId(),
								"email", u.getEmail(),
								"fullName", u.getFullName()
							)
						);
					}
				)
				.orElseGet(
					() -> ResponseEntity.status(401).body(
						Map.of("error", "Credenciales inválidas")
					)
				);
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpSession session) {
		session.invalidate();
		return ResponseEntity.ok(Map.of("ok", true));
	}

	@GetMapping("/me")
	public ResponseEntity<?> me(HttpSession session) {
		Object userId = session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).body(
				Map.of("error","No autenticado")
			);
		}
		return ResponseEntity.ok(
			Map.of(
				"id", userId,
				"fullName", session.getAttribute("fullName")
			)
		);
	}
}
