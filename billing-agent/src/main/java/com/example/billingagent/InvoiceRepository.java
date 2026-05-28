package com.example.billingagent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<InvoiceDto> findByCustomerId(String customerId);

    List<Invoice> findByCustomerIdAndStatus(String customerId, InvoiceStatus status);

    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);

    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.customerId = :customerId AND i.status = :status")
    BigDecimal sumAmountByCustomerIdAndStatus(@Param("customerId") String customerId,
                                              @Param("status") InvoiceStatus status);
}
