package com.attendance.backend.auth.service;

import com.attendance.backend.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

@Service
public class PasswordPolicyService {

    private static final int MIN_PASSWORD_LENGTH = 10;
    private static final int MAX_PASSWORD_LENGTH = 200;

    private static final Set<String> COMMON_WEAK_PASSWORDS = Set.of(
            "password",
            "password123",
            "12345678",
            "123456789",
            "qwerty123",
            "11111111",
            "admin123",
            "welcome123",
            "abc123456",
            "iloveyou"
    );

    public void validateOrThrow(String password) {
        if (!StringUtils.hasText(password)) {
            throw ApiException.badRequest("PASSWORD_REQUIRED", "password is required");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw ApiException.badRequest(
                    "PASSWORD_TOO_SHORT",
                    "password must be at least " + MIN_PASSWORD_LENGTH + " characters"
            );
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw ApiException.badRequest(
                    "PASSWORD_TOO_LONG",
                    "password must be at most " + MAX_PASSWORD_LENGTH + " characters"
            );
        }

        String normalized = password.toLowerCase(Locale.ROOT);
        if (COMMON_WEAK_PASSWORDS.contains(normalized)) {
            throw ApiException.badRequest("PASSWORD_TOO_COMMON", "password is too common");
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                hasLetter = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }

        if (!hasLetter || !hasDigit) {
            throw ApiException.badRequest(
                    "PASSWORD_TOO_WEAK",
                    "password must contain at least one letter and one digit"
            );
        }
    }
}