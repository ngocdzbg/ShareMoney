package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.event.ExpenseEvent;
import com.ngocnb.sharemoney.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyExpenseCreated(ExpenseEvent event) {

        log.info(
                "NOTIFICATION: New expense '{}' added to group {}. Amount={}",
                event.getDescription(),
                event.getGroupId(),
                event.getAmount()
        );
    }

    @Override
    public void notifyExpenseUpdated(ExpenseEvent event) {

        log.info(
                "NOTIFICATION: Expense {} was updated. Amount={}",
                event.getExpenseId(),
                event.getAmount()
        );
    }

    @Override
    public void notifyExpenseDeleted(ExpenseEvent event) {

        log.info(
                "NOTIFICATION: Expense {} was deleted from group {}",
                event.getExpenseId(),
                event.getGroupId()
        );
    }
}