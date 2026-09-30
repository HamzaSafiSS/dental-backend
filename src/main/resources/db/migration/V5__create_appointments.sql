CREATE TABLE appointments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id          UUID        NOT NULL,
    doctor_id           UUID        NOT NULL,
    service_id          UUID        NOT NULL,
    appointment_date    DATE        NOT NULL,
    start_time          TIME        NOT NULL,
    end_time            TIME        NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    reason_for_visit    TEXT,
    patient_notes       TEXT,
    doctor_notes        TEXT,
    cancellation_reason TEXT,
    cancelled_by        UUID,
    rescheduled_from    UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_appointments_patient      FOREIGN KEY (patient_id)       REFERENCES patients (id),
    CONSTRAINT fk_appointments_doctor       FOREIGN KEY (doctor_id)        REFERENCES doctors (id),
    CONSTRAINT fk_appointments_service      FOREIGN KEY (service_id)       REFERENCES services (id),
    CONSTRAINT fk_appointments_cancelled_by FOREIGN KEY (cancelled_by)     REFERENCES users (id)    ON DELETE SET NULL,
    CONSTRAINT fk_appointments_rescheduled  FOREIGN KEY (rescheduled_from) REFERENCES appointments (id) ON DELETE SET NULL,
    CONSTRAINT ck_appointments_status       CHECK (status IN (
        'PENDING_PAYMENT', 'PAYMENT_VERIFIED', 'CONFIRMED',
        'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED',
        'CANCELLED', 'NO_SHOW', 'RESCHEDULED'
    )),
    CONSTRAINT ck_appointments_time         CHECK (start_time < end_time)
);

-- Partial unique index: prevent double-booking for active appointments only
CREATE UNIQUE INDEX uq_appointment_doctor_slot
    ON appointments (doctor_id, appointment_date, start_time)
    WHERE status NOT IN ('CANCELLED', 'NO_SHOW', 'RESCHEDULED');

CREATE INDEX idx_appointments_patient_id ON appointments (patient_id);
CREATE INDEX idx_appointments_doctor_id  ON appointments (doctor_id);
CREATE INDEX idx_appointments_date       ON appointments (appointment_date);
CREATE INDEX idx_appointments_status     ON appointments (status);

-- Appointment status change history (full audit trail)
CREATE TABLE appointment_status_history (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id      UUID        NOT NULL,
    previous_status     VARCHAR(30),
    new_status          VARCHAR(30) NOT NULL,
    changed_by          UUID,
    reason              TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_status_history_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE CASCADE,
    CONSTRAINT fk_status_history_changed_by  FOREIGN KEY (changed_by)     REFERENCES users (id)        ON DELETE SET NULL
);

CREATE INDEX idx_status_history_appointment_id ON appointment_status_history (appointment_id);

-- Appointment payments (1:1 with appointments)
-- This table supports the payment-verification workflow only.
CREATE TABLE appointment_payments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id          UUID          NOT NULL,
    required_amount         DECIMAL(10,2) NOT NULL,
    payment_status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    payment_reference       VARCHAR(255),
    payment_screenshot_url  VARCHAR(500),
    paid_at                 TIMESTAMPTZ,
    verified_at             TIMESTAMPTZ,
    verified_by             UUID,
    verification_note       TEXT,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_payments_appointment   UNIQUE (appointment_id),
    CONSTRAINT fk_payments_appointment   FOREIGN KEY (appointment_id) REFERENCES appointments (id)  ON DELETE CASCADE,
    CONSTRAINT fk_payments_verified_by   FOREIGN KEY (verified_by)    REFERENCES users (id)         ON DELETE SET NULL,
    CONSTRAINT ck_payments_status        CHECK (payment_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    CONSTRAINT ck_payments_amount        CHECK (required_amount >= 0)
);
