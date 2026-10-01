package com.dental.dentalbackend.clinic.repository;

import com.dental.dentalbackend.clinic.entity.ClinicSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClinicSettingsRepository extends JpaRepository<ClinicSettings, UUID> {

    // Single-row table — always use findAll().get(0) or findFirst
    default ClinicSettings getSettings() {
        return findAll().stream().findFirst().orElse(new ClinicSettings());
    }
}
