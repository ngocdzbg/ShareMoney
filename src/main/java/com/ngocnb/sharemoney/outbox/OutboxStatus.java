package com.ngocnb.sharemoney.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}