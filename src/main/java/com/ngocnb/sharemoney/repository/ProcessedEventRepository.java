package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.idempotency.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, Long> {

    @Modifying
    @Query(
            value = """
                    INSERT IGNORE INTO processed_events
                        (consumer_name, event_id, processed_at)
                    VALUES
                        (:consumerName, :eventId, CURRENT_TIMESTAMP(6))
                    """,
            nativeQuery = true
    )
    int tryMarkAsProcessed(
            @Param("consumerName") String consumerName,
            @Param("eventId") String eventId
    );
}