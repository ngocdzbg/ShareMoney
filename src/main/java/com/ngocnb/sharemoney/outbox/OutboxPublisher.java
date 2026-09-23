package com.ngocnb.sharemoney.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Duration publishTimeout;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           @Qualifier("stringKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                           @Value("${app.outbox.publish-timeout:15s}") Duration publishTimeout) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.publishTimeout = publishTimeout;
    }

    @Scheduled(fixedDelay = 5000, scheduler = "outboxTaskScheduler")
    public void publishPendingEvents() {
        List<OutboxEvent> events =
                outboxEventRepository.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxStatus.PENDING, LocalDateTime.now());

        for (OutboxEvent event : events) {
            if (Thread.currentThread().isInterrupted() || !publish(event)) {
                // Avoid waiting for every event in the batch while the broker is unavailable.
                break;
            }
        }
    }

    private boolean publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event.getPayload())
                    .get(publishTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            // No acknowledgement: leave the persisted record PENDING for the next run.
            log.info("Outbox publishing interrupted: eventId={}", event.getEventId());
            return false;
        } catch (ExecutionException | TimeoutException | org.apache.kafka.common.KafkaException
                 | org.springframework.kafka.KafkaException exception) {
            scheduleRetry(event, exception);
            return false;
        }

        // Keep persistence failures separate from Kafka failures. If this save fails after
        // Kafka accepted the event, the persisted PENDING record may be delivered again.
        // Consumers already deduplicate using eventId (at-least-once delivery).
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        event.setNextRetryAt(null);
        event.setLastError(null);
        outboxEventRepository.save(event);
        log.info("Outbox event published: eventId={}, aggregateId={}",
                event.getEventId(), event.getAggregateId());
        return true;
    }

    private void scheduleRetry(OutboxEvent event, Exception exception) {
        int retryCount = event.getRetryCount() == Integer.MAX_VALUE
                ? Integer.MAX_VALUE : event.getRetryCount() + 1;
        long delaySeconds = 5L * (1L << Math.min(retryCount - 1, 6));
        event.setStatus(OutboxStatus.PENDING);
        event.setPublishedAt(null);
        event.setRetryCount(retryCount);
        event.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
        event.setLastError(getRootMessage(exception));
        outboxEventRepository.save(event);
        log.warn("Outbox publish failed; event remains PENDING: eventId={}, retryCount={}, nextRetryAt={}, error={}",
                event.getEventId(), retryCount, event.getNextRetryAt(), event.getLastError());
    }

    private String getRootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        if (message == null) {
            return root.getClass().getSimpleName();
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
