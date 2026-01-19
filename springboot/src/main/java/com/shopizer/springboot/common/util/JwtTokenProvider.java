package com.shopizer.springboot.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider
 * Utility for generating and validating JWT tokens
 * FR-025: Customer authentication (login/logout)
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-256-bit-secret-key-change-this-in-production-minimum-32-characters}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long customerId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(customerId.toString())
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long customerId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(customerId.toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getCustomerIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getExpirationTime() {
        return jwtExpiration;
    }
}
