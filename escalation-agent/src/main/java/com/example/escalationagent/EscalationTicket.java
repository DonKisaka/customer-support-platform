package com.example.escalationagent;

import java.time.LocalDateTime;

public record EscalationTicket(
        String ticketId,
        String customerId,
        String issue,
        String priority,    // LOW, MEDIUM, HIGH, CRITICAL
        String status,      // OPEN, IN_PROGRESS, RESOLVED
        LocalDateTime createdAt
) {}
