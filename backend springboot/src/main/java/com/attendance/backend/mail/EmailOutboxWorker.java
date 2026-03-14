package com.attendance.backend.mail;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EmailOutboxWorker {

    private final EmailOutboxProperties properties;
    private final EmailOutboxProcessorService processorService;

    public EmailOutboxWorker(EmailOutboxProperties properties,
                             EmailOutboxProcessorService processorService) {
        this.properties = properties;
        this.processorService = processorService;
    }

    @Scheduled(fixedDelayString = "${app.auth.email-outbox.poll-delay-ms:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        List<UUID> ids = processorService.claimDueIds();
        for (UUID id : ids) {
            processorService.processOne(id);
        }
    }
}