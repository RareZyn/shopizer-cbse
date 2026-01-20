package com.shopizer.springboot.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT Token Provider for Merchants (FR-015)
 */
@Component
public class JwtTokenProviderMerchant {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProviderMerchant(
            @Value("${jwt.secret:your-256-bit-secret-key-change-this-in-production-minimum-32-characters}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long merchantId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(merchantId))
                .claim("email", email)
                .claim("role", "MERCHANT")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public Long getMerchantId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
