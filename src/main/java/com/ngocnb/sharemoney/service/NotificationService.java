package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.event.ExpenseEvent;

public interface NotificationService {

    void notifyExpenseCreated(ExpenseEvent event);

    void notifyExpenseUpdated(ExpenseEvent event);

    void notifyExpenseDeleted(ExpenseEvent event);
}