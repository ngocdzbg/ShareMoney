package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.outbox.OutboxAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/outbox-events")
@RequiredArgsConstructor
public class OutboxAdminController {

    private final OutboxAdminService outboxAdminService;

    @PostMapping("/{eventId}/replay")
    public ResponseEntity<Void> replay(
            @PathVariable String eventId
    ) {
        outboxAdminService.replayFailedEvent(eventId);

        return ResponseEntity.noContent().build();
    }
}