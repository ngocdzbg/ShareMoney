package com.ngocnb.sharemoney.idempotency;

import com.ngocnb.sharemoney.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotentEventProcessor {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void process(
            String consumerName,
            String eventId,
            Runnable action
    ) {

        int inserted = processedEventRepository.tryMarkAsProcessed(
                consumerName,
                eventId
        );

        if (inserted == 0) {
            log.warn(
                    "Duplicate event skipped: consumer={}, eventId={}",
                    consumerName,
                    eventId
            );

            return;
        }

        log.debug(
                "Processing new event: consumer={}, eventId={}",
                consumerName,
                eventId
        );

        action.run();
    }
}