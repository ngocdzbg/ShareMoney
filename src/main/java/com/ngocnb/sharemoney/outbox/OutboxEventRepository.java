package com.ngocnb.sharemoney.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            LocalDateTime now
    );

    Optional<OutboxEvent> findByEventId(String eventId);
}