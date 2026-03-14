package com.attendance.backend.mail;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EmailOutboxCryptoService {

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int NONCE_LENGTH_BYTES = 12;

    private final EmailOutboxProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKey secretKey;

    public EmailOutboxCryptoService(EmailOutboxProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(properties.getEncryptionKeyBase64())) {
            throw new IllegalStateException("app.auth.email-outbox.encryption-key-base64 is required");
        }

        byte[] raw = Base64.getDecoder().decode(properties.getEncryptionKeyBase64());
        if (!(raw.length == 16 || raw.length == 24 || raw.length == 32)) {
            throw new IllegalStateException("Outbox encryption key must be 128/192/256-bit");
        }

        this.secretKey = new SecretKeySpec(raw, "AES");
    }

    public EncryptedPayload encrypt(byte[] plaintext) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH_BYTES];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext);

            return new EncryptedPayload(nonce, ciphertext);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt outbox payload", ex);
        }
    }

    public byte[] decrypt(byte[] nonce, byte[] ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
            return cipher.doFinal(ciphertext);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to decrypt outbox payload", ex);
        }
    }

    public record EncryptedPayload(byte[] nonce, byte[] ciphertext) {
    }
}