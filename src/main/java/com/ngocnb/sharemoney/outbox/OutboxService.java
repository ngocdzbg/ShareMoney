package com.ngocnb.sharemoney.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    LocalDateTime now = LocalDateTime.now();

    public void save(
            String eventId,
            String eventType,
            String aggregateType,
            Long aggregateId,
            String topic,
            String payload
    ) {

        OutboxEvent event = OutboxEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .topic(topic)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(now)
                .nextRetryAt(now)
                .build();

        outboxEventRepository.save(event);
    }
}