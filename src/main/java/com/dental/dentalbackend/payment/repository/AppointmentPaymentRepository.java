package com.dental.dentalbackend.payment.repository;

import com.dental.dentalbackend.payment.entity.AppointmentPayment;
import com.dental.dentalbackend.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentPaymentRepository extends JpaRepository<AppointmentPayment, UUID> {

    Optional<AppointmentPayment> findByAppointmentId(UUID appointmentId);

    Page<AppointmentPayment> findAllByPaymentStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM AppointmentPayment p WHERE p.paymentStatus = com.dental.dentalbackend.payment.entity.PaymentStatus.VERIFIED")
    BigDecimal sumTotalVerifiedRevenue();

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM AppointmentPayment p
            WHERE p.paymentStatus = com.dental.dentalbackend.payment.entity.PaymentStatus.VERIFIED
              AND p.verifiedAt >= :start
              AND p.verifiedAt <= :end
            """)
    BigDecimal sumVerifiedRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
