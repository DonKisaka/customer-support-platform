package com.example.billingagent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceDto(
        Long id,
        String customerId,
        String customerName,
        BigDecimal amount,
        InvoiceStatus status,
        LocalDate issueDate,
        LocalDate dueDate
) {
    public static InvoiceDto from(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.getCustomerId(),
                invoice.getCustomerName(),
                invoice.getAmount(),
                invoice.getStatus(),
                invoice.getIssueDate(),
                invoice.getDueDate()
        );
    }
}
