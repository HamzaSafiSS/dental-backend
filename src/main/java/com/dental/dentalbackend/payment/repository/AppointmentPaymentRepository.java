package com.dental.dentalbackend.payment.repository;

import com.dental.dentalbackend.payment.entity.AppointmentPayment;
import com.dental.dentalbackend.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentPaymentRepository extends JpaRepository<AppointmentPayment, UUID> {

    Optional<AppointmentPayment> findByAppointmentId(UUID appointmentId);

    Page<AppointmentPayment> findAllByPaymentStatus(PaymentStatus status, Pageable pageable);
}
