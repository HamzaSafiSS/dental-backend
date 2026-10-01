package com.dental.dentalbackend.notification.repository;

import com.dental.dentalbackend.notification.entity.Notification;
import com.dental.dentalbackend.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByUserId(UUID userId, Pageable pageable);

    List<Notification> findAllByStatus(NotificationStatus status);

    Page<Notification> findAllByReferenceTypeAndReferenceId(String referenceType, UUID referenceId, Pageable pageable);
}
