package com.metrotecnica.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-hours}")
    private long expirationHours;

    private SecretKey getSigningKey() {
        // Repete/derriva a chave para garantir tamanho mínimo exigido pelo HS256 (256 bits)
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role, Long tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("tenant_id", tenantId);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationHours * 3600 * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractTenantId(String token) {
        Object tenantId = extractAllClaims(token).get("tenant_id");
        return tenantId != null ? ((Number) tenantId).longValue() : null;
    }

    public boolean isTokenValid(String token, String expectedEmail) {
        final String email = extractEmail(token);
        return email.equals(expectedEmail) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}