package com.attendance.backend.mail;

import java.time.Instant;

public interface MailService {

    void sendPasswordResetEmail(String toEmail,
                                String fullName,
                                String resetUrl,
                                Instant expiresAt);
}