-- ============================================================
-- V2: Patient-related tables
-- ============================================================

-- Patient profiles linked 1:1 to users
CREATE TABLE patients (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL,
    date_of_birth       DATE,
    gender              VARCHAR(10),
    secondary_phone     VARCHAR(20),
    address             TEXT,
    city                VARCHAR(100),
    state               VARCHAR(100),
    zip_code            VARCHAR(20),
    blood_type          VARCHAR(5),
    profile_image_url   VARCHAR(500),
    notes               TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_patients_user_id UNIQUE (user_id),
    CONSTRAINT fk_patients_user    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_patients_gender  CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- Patient emergency contacts
CREATE TABLE patient_emergency_contacts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID         NOT NULL,
    name            VARCHAR(200) NOT NULL,
    relationship    VARCHAR(50),
    phone           VARCHAR(20)  NOT NULL,
    email           VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_emergency_contacts_patient FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE
);

CREATE INDEX idx_emergency_contacts_patient_id ON patient_emergency_contacts (patient_id);

-- Patient medical history
CREATE TABLE patient_medical_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID         NOT NULL,
    condition       VARCHAR(255) NOT NULL,
    description     TEXT,
    diagnosed_date  DATE,
    is_current      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_medical_history_patient FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE
);

CREATE INDEX idx_medical_history_patient_id ON patient_medical_history (patient_id);

-- Patient allergies
CREATE TABLE patient_allergies (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id  UUID         NOT NULL,
    allergen    VARCHAR(255) NOT NULL,
    severity    VARCHAR(20),
    reaction    TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_allergies_patient     FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE,
    CONSTRAINT ck_allergies_severity    CHECK (severity IS NULL OR severity IN ('MILD', 'MODERATE', 'SEVERE'))
);

CREATE INDEX idx_allergies_patient_id ON patient_allergies (patient_id);

-- Patient documents (X-rays, lab results, referrals, etc.)
CREATE TABLE patient_documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID         NOT NULL,
    uploaded_by     UUID,
    document_name   VARCHAR(255) NOT NULL,
    document_type   VARCHAR(50),
    file_url        VARCHAR(500) NOT NULL,
    file_size       BIGINT,
    mime_type       VARCHAR(100),
    description     TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_documents_patient     FOREIGN KEY (patient_id)  REFERENCES patients (id) ON DELETE CASCADE,
    CONSTRAINT fk_documents_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id)    ON DELETE SET NULL,
    CONSTRAINT ck_documents_type        CHECK (document_type IS NULL OR document_type IN ('X_RAY', 'LAB_RESULT', 'REFERRAL', 'CONSENT', 'INSURANCE', 'OTHER'))
);

CREATE INDEX idx_documents_patient_id ON patient_documents (patient_id);
