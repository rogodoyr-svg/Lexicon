package com.lexicon.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long expirationHours;

	public JwtService(
		@Value("${security.jwt.secret}") String secret,
		@Value("${security.jwt.expiration-hours}") long expirationHours
	) {
		this.signingKey = Keys.hmacShaKeyFor(sha256(secret));
		this.expirationHours = expirationHours;
	}

	private byte[] sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("No fue posible inicializar SHA-256", exception);
		}
	}

	public String generateToken(String email) {
		Instant now = Instant.now();
		Instant exp = now.plus(Duration.ofHours(expirationHours));

		return Jwts.builder()
			.subject(email)
			.issuedAt(Date.from(now))
			.expiration(Date.from(exp))
			.signWith(signingKey)
			.compact();
	}
}
