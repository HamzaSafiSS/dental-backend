CREATE TABLE dental_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id          UUID        NOT NULL,
    doctor_id           UUID        NOT NULL,
    appointment_id      UUID,
    record_type         VARCHAR(50),
    chief_complaint     TEXT,
    clinical_findings   TEXT,
    diagnosis           TEXT,
    treatment_performed TEXT,
    notes               TEXT,
    tooth_number        VARCHAR(10),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_dental_records_patient     FOREIGN KEY (patient_id)     REFERENCES patients (id),
    CONSTRAINT fk_dental_records_doctor      FOREIGN KEY (doctor_id)      REFERENCES doctors (id),
    CONSTRAINT fk_dental_records_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE SET NULL,
    CONSTRAINT ck_dental_records_type        CHECK (record_type IS NULL OR record_type IN ('EXAMINATION', 'TREATMENT', 'FOLLOW_UP', 'EMERGENCY'))
);

CREATE INDEX idx_dental_records_patient_id     ON dental_records (patient_id);
CREATE INDEX idx_dental_records_doctor_id      ON dental_records (doctor_id);
CREATE INDEX idx_dental_records_appointment_id ON dental_records (appointment_id);

-- Treatment plans
CREATE TABLE treatment_plans (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id              UUID          NOT NULL,
    doctor_id               UUID          NOT NULL,
    appointment_id          UUID,
    title                   VARCHAR(255)  NOT NULL,
    description             TEXT,
    status                  VARCHAR(20)   NOT NULL DEFAULT 'PROPOSED',
    start_date              DATE,
    estimated_end_date      DATE,
    total_estimated_cost    DECIMAL(10,2),
    notes                   TEXT,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_treatment_plans_patient     FOREIGN KEY (patient_id)     REFERENCES patients (id),
    CONSTRAINT fk_treatment_plans_doctor      FOREIGN KEY (doctor_id)      REFERENCES doctors (id),
    CONSTRAINT fk_treatment_plans_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE SET NULL,
    CONSTRAINT ck_treatment_plans_status      CHECK (status IN ('PROPOSED', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_treatment_plans_patient_id ON treatment_plans (patient_id);
CREATE INDEX idx_treatment_plans_doctor_id  ON treatment_plans (doctor_id);

-- Treatment plan items (individual procedures within a plan)
CREATE TABLE treatment_plan_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treatment_plan_id   UUID          NOT NULL,
    service_id          UUID,
    tooth_number        VARCHAR(10),
    description         TEXT          NOT NULL,
    sequence_order      INTEGER       NOT NULL DEFAULT 0,
    estimated_cost      DECIMAL(10,2),
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    notes               TEXT,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_plan_items_plan    FOREIGN KEY (treatment_plan_id) REFERENCES treatment_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_items_service FOREIGN KEY (service_id)        REFERENCES services (id)        ON DELETE SET NULL,
    CONSTRAINT ck_plan_items_status  CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_plan_items_plan_id ON treatment_plan_items (treatment_plan_id);

-- Prescriptions
CREATE TABLE prescriptions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id          UUID        NOT NULL,
    doctor_id           UUID        NOT NULL,
    appointment_id      UUID,
    prescription_date   DATE        NOT NULL DEFAULT CURRENT_DATE,
    diagnosis           TEXT,
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_prescriptions_patient     FOREIGN KEY (patient_id)     REFERENCES patients (id),
    CONSTRAINT fk_prescriptions_doctor      FOREIGN KEY (doctor_id)      REFERENCES doctors (id),
    CONSTRAINT fk_prescriptions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE SET NULL
);

CREATE INDEX idx_prescriptions_patient_id ON prescriptions (patient_id);
CREATE INDEX idx_prescriptions_doctor_id  ON prescriptions (doctor_id);

-- Prescription items (individual medications)
CREATE TABLE prescription_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_id     UUID         NOT NULL,
    medication_name     VARCHAR(255) NOT NULL,
    dosage              VARCHAR(100),
    frequency           VARCHAR(100),
    duration            VARCHAR(100),
    quantity            INTEGER,
    instructions        TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_prescription_items_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions (id) ON DELETE CASCADE
);

CREATE INDEX idx_prescription_items_prescription_id ON prescription_items (prescription_id);
