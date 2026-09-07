package com.ngocnb.sharemoney.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxAdminService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void replayFailedEvent(String eventId) {
        OutboxEvent event = outboxEventRepository.findByEventId(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Outbox event not found: " + eventId)
                );

        if (event.getStatus() != OutboxStatus.FAILED) {
            throw new IllegalStateException(
                    "Only FAILED event can be replayed"
            );
        }

        event.setStatus(OutboxStatus.PENDING);
        event.setRetryCount(0);
        event.setNextRetryAt(LocalDateTime.now());
        event.setLastError(null);
        event.setPublishedAt(null);

        outboxEventRepository.save(event);
    }
}