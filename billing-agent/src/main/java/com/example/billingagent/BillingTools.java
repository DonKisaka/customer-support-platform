package com.example.billingagent;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingTools {

    private final InvoiceRepository invoiceRepository;

    @Tool(description = "Get all invoices for a customer by customer ID")
    @Transactional(readOnly = true)
    public List<InvoiceDto> getInvoicesByCustomer(
            @ToolParam(description = "The customer ID") String customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    @Tool(description = "Get the status of a specific invoice by invoice ID")
    @Transactional(readOnly = true)
    public String getInvoiceStatus(
            @ToolParam(description = "The invoice ID") Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(i -> "Invoice #" + i.getId() + " | Amount: $" + i.getAmount() +
                          " | Status: " + i.getStatus() + " | Date: " + i.getDueDate())
                .orElse("Invoice not found with ID: " + invoiceId);
    }

    @Tool(description = "Get total outstanding (unpaid) balance for a customer")
    @Transactional(readOnly = true)
    public String getOutstandingBalance(
            @ToolParam(description = "The customer ID") String customerId) {
        BigDecimal total = invoiceRepository
                .sumAmountByCustomerIdAndStatus(customerId, InvoiceStatus.UNPAID);

        total = (total != null) ? total : BigDecimal.ZERO;
        return "Outstanding balance for customer %s: %s"
                .formatted(customerId, NumberFormat.getCurrencyInstance().format(total));
    }

    @Tool(description = "Get all invoices for a customer filtered by a specific status (PAID, UNPAID, REFUNDED)")
    @Transactional(readOnly = true)
    public List<InvoiceDto> getInvoicesByCustomerAndStatus(
            @ToolParam(description = "The customer ID") String customerId,
            @ToolParam(description = "The invoice status: PAID, UNPAID, or REFUNDED") InvoiceStatus status) {
        return invoiceRepository.findByCustomerIdAndStatus(customerId, status)
                .stream()
                .map(InvoiceDto::from)
                .toList();
    }

    @Tool(description = "Get all invoices across all customers by a specific status. " +
            "Use when you need a global view of PAID, UNPAID, or REFUNDED invoices.")
    @Transactional(readOnly = true)
    public List<InvoiceDto> getAllInvoicesByStatus(
            @ToolParam(description = "The invoice status: PAID, UNPAID, or REFUNDED") InvoiceStatus status) {
        return invoiceRepository.findByStatus(status)
                .stream()
                .map(InvoiceDto::from)
                .toList();
    }

    @Tool(description = "List all overdue unpaid invoices for a customer. " +
            "Use when checking if a customer has missed payment deadlines.")
    @Transactional(readOnly = true)
    public List<InvoiceDto> getOverdueInvoices(
            @ToolParam(description = "The unique customer ID") String customerId) {

        return invoiceRepository
                .findByStatusAndDueDateBefore(InvoiceStatus.UNPAID, LocalDate.now())
                .stream()
                .filter(i -> i.getCustomerId().equals(customerId))
                .map(InvoiceDto::from)
                .toList();
    }


}
