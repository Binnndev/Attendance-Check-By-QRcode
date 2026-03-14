package com.attendance.backend.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@Profile("!dev")
public class SmtpMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

    private final JavaMailSender mailSender;
    private final AppMailProperties mailProperties;

    public SmtpMailService(JavaMailSender mailSender, AppMailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail,
                                       String fullName,
                                       String resetUrl,
                                       Instant expiresAt) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(toEmail);
            helper.setSubject("Reset your password");

            if (StringUtils.hasText(mailProperties.getFromAddress())) {
                String fromName = StringUtils.hasText(mailProperties.getFromName())
                        ? mailProperties.getFromName()
                        : "Attendance System";
                helper.setFrom(new InternetAddress(mailProperties.getFromAddress(), fromName));
            }

            helper.setText(
                    buildTextBody(fullName, resetUrl, expiresAt),
                    buildHtmlBody(fullName, resetUrl, expiresAt)
            );

            mailSender.send(mimeMessage);
            log.info("Password reset email sent successfully");
        } catch (Exception ex) {
            log.error("Failed to send password reset email", ex);
            throw new IllegalStateException("Failed to send password reset email", ex);
        }
    }

    private String buildTextBody(String fullName, String resetUrl, Instant expiresAt) {
        String displayName = StringUtils.hasText(fullName) ? fullName : "User";
        return """
                Hello %s,

                We received a request to reset your password.

                Use the link below to set a new password:
                %s

                This link expires at: %s

                If you did not request this, you can ignore this email.

                Regards,
                Attendance System
                """.formatted(displayName, resetUrl, DATE_TIME_FORMATTER.format(expiresAt));
    }

    private String buildHtmlBody(String fullName, String resetUrl, Instant expiresAt) {
        String displayName = HtmlUtils.htmlEscape(StringUtils.hasText(fullName) ? fullName : "User");
        String escapedUrl = HtmlUtils.htmlEscape(resetUrl);
        String expiresText = HtmlUtils.htmlEscape(DATE_TIME_FORMATTER.format(expiresAt));

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #1f2937;">
                    <p>Hello <strong>%s</strong>,</p>
                    <p>We received a request to reset your password.</p>
                    <p>
                      <a href="%s"
                         style="display:inline-block;padding:12px 18px;background:#2563eb;color:#ffffff;text-decoration:none;border-radius:6px;">
                        Reset password
                      </a>
                    </p>
                    <p>If the button does not work, use this link:</p>
                    <p><a href="%s">%s</a></p>
                    <p>This link expires at: <strong>%s</strong></p>
                    <p>If you did not request this, you can safely ignore this email.</p>
                    <p>Regards,<br/>Attendance System</p>
                  </body>
                </html>
                """.formatted(displayName, escapedUrl, escapedUrl, escapedUrl, expiresText);
    }
}