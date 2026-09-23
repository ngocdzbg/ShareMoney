package com.ngocnb.sharemoney.kafka;

import com.ngocnb.sharemoney.event.ExpenseEvent;
import com.ngocnb.sharemoney.idempotency.IdempotentEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ExpenseAuditConsumer {

    private static final String CONSUMER_NAME =
            "expense-audit-group";

    private final IdempotentEventProcessor idempotentEventProcessor;

    @KafkaListener(
            topics = "expense-events",
            groupId = "expense-audit-group"
    )
    public void consume(ExpenseEvent event) {

        log.info(
                "Received audit event: eventId={}, type={}, expenseId={}",
                event.getEventId(),
                event.getEventType(),
                event.getExpenseId()
        );

        idempotentEventProcessor.process(
                CONSUMER_NAME,
                event.getEventId(),
                () -> writeAuditLog(event)
        );
    }

    private void writeAuditLog(ExpenseEvent event) {

        log.info(
                """
                        ===== EXPENSE AUDIT EVENT =====
                        Event ID: {}
                        Event Type: {}
                        Expense ID: {}
                        Group ID: {}
                        Amount: {}
                        Description: {}
                        Occurred At: {}
                        ===============================
                        """,
                event.getEventId(),
                event.getEventType(),
                event.getExpenseId(),
                event.getGroupId(),
                event.getAmount(),
                event.getDescription(),
                event.getOccurredAt()
        );
    }
}
