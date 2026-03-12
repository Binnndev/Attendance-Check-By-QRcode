package com.attendance.backend.security.jwt;

import com.attendance.backend.auth.repository.UserSessionRepository;
import com.attendance.backend.domain.entity.UserSession;
import com.attendance.backend.security.JwtUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;
    private final Clock clock;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserSessionRepository userSessionRepository,
                                   Clock clock) {
        this.jwtService = jwtService;
        this.userSessionRepository = userSessionRepository;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null) {
            return false;
        }

        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh")
                || path.equals("/api/v1/auth/logout")
                || path.equals("/actuator/health")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (token.isBlank()) {
            writeUnauthorized(response);
            return;
        }

        try {
            JwtService.ParsedJwt parsed = jwtService.parseAndValidate(token);

            if (!JwtService.TOKEN_TYPE_ACCESS.equals(parsed.type())) {
                writeUnauthorized(response);
                return;
            }

            Instant now = Instant.now(clock);

            UserSession session = userSessionRepository
                    .findActiveById(parsed.sessionId(), now)
                    .orElse(null);

            if (session == null) {
                writeUnauthorized(response);
                return;
            }

            if (!session.getUserId().equals(parsed.userId())) {
                writeUnauthorized(response);
                return;
            }

            String primaryRole = parsed.roles().isEmpty() ? null : parsed.roles().get(0);
            JwtUserPrincipal principal = new JwtUserPrincipal(
                    parsed.userId(),
                    parsed.sessionId(),
                    primaryRole,
                    parsed.roles()
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response);
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Missing/invalid token\"}");
    }
}