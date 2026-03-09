package com.attendance.backend.security;

import java.util.UUID;

public interface UserPrincipal {
    UUID getUserId();

    String getRole();
}
