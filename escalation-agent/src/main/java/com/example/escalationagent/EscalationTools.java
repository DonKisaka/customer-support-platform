package com.example.escalationagent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EscalationTools {

    private final Map<String, EscalationTicket> tickets = new ConcurrentHashMap<>();

    @Tool(description = "Create an escalation ticket for a customer issue that requires human attention. " +
            "Use this when the issue cannot be resolved automatically.")
    public EscalationTicket createEscalationTicket(
            @ToolParam(description = "The customer ID") String customerId,
            @ToolParam(description = "A clear description of the issue") String issue,
            @ToolParam(description = "Priority level: LOW, MEDIUM, HIGH, or CRITICAL") String priority) {

        String ticketId = "ESC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        EscalationTicket ticket = new EscalationTicket(
                ticketId, customerId, issue, priority, "OPEN", LocalDateTime.now()
        );
        tickets.put(ticketId, ticket);
        log.info("Created escalation ticket: {} for customer: {}", ticketId, customerId);
        return ticket;
    }

    @Tool(description = "Get the current status of an escalation ticket by ticket ID")
    public String getEscalationTicketStatus(
            @ToolParam(description = "The escalation ticket ID") String ticketId) {

        return Optional.ofNullable(tickets.get(ticketId))
                .map(t -> "Ticket %s | Customer: %s | Priority: %s | Status: %s | Created: %s"
                        .formatted(t.ticketId(), t.customerId(), t.priority(), t.status(), t.createdAt()))
                .orElse("Ticket not found: " + ticketId);
    }

    @Tool(description = "List all open escalation tickets for a customer")
    public String getOpenTicketsByCustomer(
            @ToolParam(description = "The customer ID") String customerId) {

        String result = tickets.values().stream()
                .filter(t -> t.customerId().equals(customerId) && t.status().equals("OPEN"))
                .map(t -> "- %s | %s | Priority: %s | Created: %s"
                        .formatted(t.ticketId(), t.issue(), t.priority(), t.createdAt()))
                .reduce("", (a, b) -> a + "\n" + b);

        return result.isBlank()
                ? "No open tickets found for customer: " + customerId
                : "Open tickets for customer " + customerId + ":\n" + result;
    }
}
