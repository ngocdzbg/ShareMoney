package com.ngocnb.sharemoney.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final int MAX_RETRY = 3;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxStatus.PENDING,
                        LocalDateTime.now()
                );

        for (OutboxEvent event : events) {
            publish(event);
        }
    }

    private LocalDateTime calculateNextRetry(int retryCount) {
        long delaySeconds =
                5L * (1L << Math.min(retryCount - 1, 6));

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    private void publish(OutboxEvent event) {

        log.info(
                "Publishing outbox event: id={}, eventId={}, aggregateId={}, status={}, retryCount={}",
                event.getId(),
                event.getEventId(),
                event.getAggregateId(),
                event.getStatus(),
                event.getRetryCount()
        );

        try {

            kafkaTemplate
                    .send(
                            event.getTopic(),
                            event.getAggregateId().toString(),
                            event.getPayload()
                    )
                    .get();

            log.info(
                    "Kafka send success: eventId={}, aggregateId={}",
                    event.getEventId(),
                    event.getAggregateId()
            );

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setNextRetryAt(null);
            event.setLastError(null);

            outboxEventRepository.save(event);

            log.info(
                    "Outbox event published: eventId={}, aggregateId={}",
                    event.getEventId(),
                    event.getAggregateId()
            );

        } catch (Exception e) {
            int retryCount = event.getRetryCount() + 1;
            String errorMessage = getRootMessage(e);

            event.setRetryCount(retryCount);
            event.setLastError(errorMessage);

            if (retryCount >= MAX_RETRY) {
                event.setStatus(OutboxStatus.FAILED);
                event.setNextRetryAt(null);

                log.error(
                        "Outbox event permanently failed: eventId={}, aggregateId={}, retryCount={}, error={}",
                        event.getEventId(),
                        event.getAggregateId(),
                        retryCount,
                        errorMessage
                );
            } else {
                LocalDateTime nextRetryAt = calculateNextRetry(retryCount);

                event.setNextRetryAt(nextRetryAt);

                log.error(
                        "Outbox publish failed: eventId={}, retryCount={}, nextRetryAt={}, error={}",
                        event.getEventId(),
                        retryCount,
                        nextRetryAt,
                        errorMessage
                );
            }

            outboxEventRepository.save(event);
        }
    }

    private String getRootMessage(Throwable throwable) {

        Throwable root = throwable;

        while (root.getCause() != null) {
            root = root.getCause();
        }

        String message = root.getMessage();

        if (message == null) {
            return root.getClass().getSimpleName();
        }

        return message.length() > 1000
                ? message.substring(0, 1000)
                : message;
    }

}