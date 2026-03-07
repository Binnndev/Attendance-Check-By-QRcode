package com.attendance.backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

public class JwtUserPrincipal implements UserPrincipal {

    private final UUID userId;
    private final String role;
    private final List<String> roles;

    public JwtUserPrincipal(UUID userId, String role, List<String> roles) {
        this.userId = userId;
        this.role = role;
        this.roles = roles == null ? List.of() : List.copyOf(roles);
    }

    @Override
    public UUID getUserId() {
        return userId;
    }

    @Override
    public String getRole() {
        return role;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_USER, ROLE_ADMIN...
        return roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}