package com.attendance.backend.security;

import java.util.UUID;

public class JwtUserPrincipal implements UserPrincipal {

    private final UUID userId;
    private final String role;

    public JwtUserPrincipal(UUID userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public UUID getUserId() {
        return userId;
    }

    @Override
    public String getRole() {
        return role;
    }
}
