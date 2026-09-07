package com.ngocnb.sharemoney.kafka;

import com.ngocnb.sharemoney.event.ExpenseEvent;
import com.ngocnb.sharemoney.idempotency.IdempotentEventProcessor;
import com.ngocnb.sharemoney.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseNotificationConsumer {

    private static final String CONSUMER_NAME =
            "expense-notification-group";

    private final NotificationService notificationService;
    private final IdempotentEventProcessor idempotentEventProcessor;

    @KafkaListener(
            topics = "expense-events",
            groupId = "expense-notification-group"
    )
    public void consume(ExpenseEvent event) {

        log.info(
                "Received expense event: eventId={}, type={}, expenseId={}",
                event.getEventId(),
                event.getEventType(),
                event.getExpenseId()
        );

        idempotentEventProcessor.process(
                CONSUMER_NAME,
                event.getEventId(),
                () -> handleEvent(event)
        );
    }

    private void handleEvent(ExpenseEvent event) {
        switch (event.getEventType()) {

            case EXPENSE_CREATED -> notificationService.notifyExpenseCreated(event);

            case EXPENSE_UPDATED -> notificationService.notifyExpenseUpdated(event);

            case EXPENSE_DELETED -> notificationService.notifyExpenseDeleted(event);
        }
    }
}