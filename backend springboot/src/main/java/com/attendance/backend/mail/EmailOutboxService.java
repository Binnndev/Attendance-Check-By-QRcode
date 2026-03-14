package com.attendance.backend.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class EmailOutboxService {

    private final EmailOutboxRepository repository;
    private final EmailOutboxCryptoService cryptoService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public EmailOutboxService(EmailOutboxRepository repository,
                              EmailOutboxCryptoService cryptoService,
                              ObjectMapper objectMapper,
                              Clock clock) {
        this.repository = repository;
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public void enqueuePasswordResetEmail(UUID passwordResetTokenId,
                                          String toEmail,
                                          String fullName,
                                          String resetUrl,
                                          Instant expiresAt) {
        try {
            PasswordResetMailPayload payload = new PasswordResetMailPayload(fullName, resetUrl, expiresAt);
            byte[] plain = objectMapper.writeValueAsBytes(payload);
            EmailOutboxCryptoService.EncryptedPayload encrypted = cryptoService.encrypt(plain);

            EmailOutbox row = new EmailOutbox();
            row.id = UUID.randomUUID();
            row.aggregateType = "PASSWORD_RESET";
            row.aggregateId = passwordResetTokenId;
            row.toEmail = toEmail;
            row.subject = "Reset your password";
            row.payloadNonce = encrypted.nonce();
            row.payloadCiphertext = encrypted.ciphertext();
            row.status = EmailOutbox.STATUS_PENDING;
            row.retryCount = 0;
            row.nextAttemptAt = Instant.now(clock);

            repository.save(row);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to enqueue password reset email", ex);
        }
    }

    public PasswordResetMailPayload readPasswordResetPayload(EmailOutbox row) {
        try {
            byte[] plain = cryptoService.decrypt(row.payloadNonce, row.payloadCiphertext);
            return objectMapper.readValue(plain, PasswordResetMailPayload.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read outbox payload", ex);
        }
    }

    public record PasswordResetMailPayload(
            String fullName,
            String resetUrl,
            Instant expiresAt
    ) {
    }
}