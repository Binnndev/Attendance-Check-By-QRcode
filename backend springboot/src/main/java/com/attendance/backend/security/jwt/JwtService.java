package com.attendance.backend.security.jwt;

import com.attendance.backend.domain.entity.User;
import com.attendance.backend.domain.enums.PlatformRole;
import com.attendance.backend.security.JwtUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class JwtService {

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final Clock clock;

    public JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Transitional method.
     * Chỉ giữ tạm để tránh compile đỏ nếu còn chỗ cũ gọi.
     * Sau khi AuthService được migrate sang session lifecycle, nên bỏ method này.
     */
    public AccessTokenResult issueAccessToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("user and user.id must not be null");
        }

        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now(clock);
        Instant exp = accessTokenExpiresAt(now);
        String accessToken = generateToken(
                user.getId(),
                sessionId,
                resolveRoles(user),
                TOKEN_TYPE_ACCESS,
                now,
                exp
        );

        return new AccessTokenResult(accessToken, exp);
    }

    public TokenBundle issueTokenBundle(User user, UUID sessionId) {
        return issueTokenBundle(user, sessionId, Instant.now(clock));
    }

    public TokenBundle issueTokenBundle(User user, UUID sessionId, Instant now) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("user and user.id must not be null");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }

        List<String> roles = resolveRoles(user);

        Instant accessExp = accessTokenExpiresAt(now);
        Instant refreshExp = refreshTokenExpiresAt(now);

        String accessToken = generateToken(
                user.getId(),
                sessionId,
                roles,
                TOKEN_TYPE_ACCESS,
                now,
                accessExp
        );

        String refreshToken = generateToken(
                user.getId(),
                sessionId,
                roles,
                TOKEN_TYPE_REFRESH,
                now,
                refreshExp
        );

        return new TokenBundle(
                accessToken,
                accessExp,
                refreshToken,
                refreshExp,
                sessionId
        );
    }

    public ParsedJwt parseAndValidate(String token) {
        Jws<Claims> jws = Jwts.parser()
                .requireIssuer(properties.getIssuer())
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token);

        Claims claims = jws.getPayload();

        UUID userId = extractUserId(claims);
        UUID sessionId = extractSessionId(claims);
        String type = claims.get("type", String.class);
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Missing token type");
        }

        Date expiration = claims.getExpiration();
        if (expiration == null) {
            throw new IllegalArgumentException("Missing token expiration");
        }

        List<String> roles = extractRoles(claims);

        return new ParsedJwt(
                userId,
                sessionId,
                roles,
                type,
                expiration.toInstant()
        );
    }

    public JwtUserPrincipal parseAccessToken(String token) {
        ParsedJwt parsed = parseAndValidate(token);
        if (!TOKEN_TYPE_ACCESS.equals(parsed.type())) {
            throw new IllegalArgumentException("Expected access token");
        }

        String primaryRole = parsed.roles().isEmpty() ? null : parsed.roles().get(0);
        return new JwtUserPrincipal(
                parsed.userId(),
                parsed.sessionId(),
                primaryRole,
                parsed.roles()
        );
    }

    public Instant accessTokenExpiresAt() {
        return accessTokenExpiresAt(Instant.now(clock));
    }

    public Instant accessTokenExpiresAt(Instant now) {
        return now.plus(properties.getAccessTokenMinutes(), ChronoUnit.MINUTES);
    }

    public Instant refreshTokenExpiresAt() {
        return refreshTokenExpiresAt(Instant.now(clock));
    }

    public Instant refreshTokenExpiresAt(Instant now) {
        return now.plus(properties.getRefreshTokenDays(), ChronoUnit.DAYS);
    }

    public byte[] hashRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null) {
            throw new IllegalArgumentException("rawRefreshToken must not be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    public boolean matchesRefreshTokenHash(String rawRefreshToken, byte[] expectedHash) {
        if (rawRefreshToken == null || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(hashRefreshToken(rawRefreshToken), expectedHash);
    }

    private String generateToken(UUID userId,
                                 UUID sessionId,
                                 Collection<String> roles,
                                 String type,
                                 Instant now,
                                 Instant exp) {

        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("token type must not be blank");
        }

        List<String> normalizedRoles = normalizeRoles(roles);
        String primaryRole = normalizedRoles.isEmpty() ? null : normalizedRoles.get(0);

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("uid", userId.toString())   // transitional compatibility
                .claim("sid", sessionId.toString())
                .claim("type", type)
                .claim("roles", normalizedRoles)
                .claim("role", primaryRole)
                .signWith(signingKey())
                .compact();
    }

    private UUID extractUserId(Claims claims) {
        String sub = claims.getSubject();
        if (sub != null && !sub.isBlank()) {
            return UUID.fromString(sub);
        }

        String uid = claims.get("uid", String.class);
        if (uid != null && !uid.isBlank()) {
            return UUID.fromString(uid);
        }

        throw new IllegalArgumentException("Missing user id");
    }

    private UUID extractSessionId(Claims claims) {
        String sid = claims.get("sid", String.class);
        if (sid == null || sid.isBlank()) {
            throw new IllegalArgumentException("Missing session id");
        }
        return UUID.fromString(sid);
    }

    private List<String> extractRoles(Claims claims) {
        Set<String> roles = new LinkedHashSet<>();

        Object rawRoles = claims.get("roles");
        if (rawRoles instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item != null) {
                    String role = item.toString().trim();
                    if (!role.isBlank()) {
                        roles.add(role);
                    }
                }
            }
        }

        String role = claims.get("role", String.class);
        if (role != null && !role.isBlank()) {
            roles.add(role.trim());
        }

        return new ArrayList<>(roles);
    }

    private List<String> resolveRoles(User user) {
        PlatformRole platformRole = (user.getPlatformRole() != null)
                ? user.getPlatformRole()
                : PlatformRole.USER;

        return List.of(platformRole.name());
    }

    private List<String> normalizeRoles(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        Set<String> out = new LinkedHashSet<>();
        for (String role : roles) {
            if (role == null) {
                continue;
            }
            String trimmed = role.trim();
            if (!trimmed.isBlank()) {
                out.add(trimmed);
            }
        }
        return new ArrayList<>(out);
    }

    private SecretKey signingKey() {
        byte[] keyBytes = decodeSecret(properties.getSecret());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret too short. Need >= 32 bytes for HS256.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String secret) {
        if (secret == null) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ignore) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public record ParsedJwt(
            UUID userId,
            UUID sessionId,
            List<String> roles,
            String type,
            Instant expiresAt
    ) {
    }

    public record AccessTokenResult(
            String accessToken,
            Instant expiresAt
    ) {
    }

    public record TokenBundle(
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            UUID sessionId
    ) {
    }
}