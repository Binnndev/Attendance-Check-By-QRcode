package com.attendance.backend.security.jwt;

import com.attendance.backend.security.JwtUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final Clock clock = Clock.systemUTC();

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateAccessToken(UUID userId, Collection<String> roles) {
        Instant now = Instant.now(clock);
        Instant exp = accessTokenExpiresAt(now);

        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId.toString());
        claims.put("roles", roles == null ? List.of() : new ArrayList<>(roles));

        String primaryRole = (roles == null || roles.isEmpty()) ? null : roles.iterator().next();
        claims.put("role", primaryRole);

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(signingKey())
                .compact();
    }

    public String generateAccessToken(UUID userId, String role) {
        return generateAccessToken(userId, role == null ? List.of() : List.of(role));
    }

    public Instant accessTokenExpiresAt() {
        return accessTokenExpiresAt(Instant.now(clock));
    }

    public Instant accessTokenExpiresAt(Instant now) {
        return now.plus(properties.getAccessTokenMinutes(), ChronoUnit.MINUTES);
    }

    public ParsedJwt parseAndValidate(String token) {
        Jws<Claims> jws = Jwts.parser()
                .requireIssuer(properties.getIssuer())
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token);

        Claims c = jws.getPayload();
        UUID uid = UUID.fromString(c.get("uid", String.class));

        List<String> roles = new ArrayList<>();
        Object rawRoles = c.get("roles");
        if (rawRoles instanceof Collection<?> col) {
            for (Object o : col) if (o != null) roles.add(o.toString());
        } else {
            String role = c.get("role", String.class);
            if (role != null) roles.add(role);
        }

        return new ParsedJwt(uid, roles);
    }

    public JwtUserPrincipal parseAccessToken(String token) {
        ParsedJwt parsed = parseAndValidate(token);
        String role = parsed.roles().isEmpty() ? null : parsed.roles().get(0);
        return new JwtUserPrincipal(parsed.userId(), role, parsed.roles());
    }

    private SecretKey signingKey() {
        byte[] keyBytes = decodeSecret(properties.getSecret());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret too short. Need >= 32 bytes for HS256.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String secret) {
        if (secret == null) return new byte[0];
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ignore) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public record ParsedJwt(UUID userId, List<String> roles) {}
}