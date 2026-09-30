-- ============================================================
-- V9: Clinic settings and audit logs
-- ============================================================

-- Clinic settings (single-row table for clinic information)
CREATE TABLE clinic_settings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_name         VARCHAR(255),
    address             TEXT,
    city                VARCHAR(100),
    state               VARCHAR(100),
    zip_code            VARCHAR(20),
    phone               VARCHAR(20),
    email               VARCHAR(255),
    emergency_contact   VARCHAR(20),
    whatsapp            VARCHAR(20),
    opening_hours       TEXT,
    latitude            DECIMAL(10,8),
    longitude           DECIMAL(11,8),
    logo_url            VARCHAR(500),
    website_url         VARCHAR(500),
    about               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed an empty clinic settings row
INSERT INTO clinic_settings (clinic_name) VALUES ('Dental Clinic');

-- Audit logs for security and business-sensitive actions
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(100),
    entity_id       UUID,
    description     TEXT,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    old_values      TEXT,
    new_values      TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_user_id     ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action      ON audit_logs (action);
CREATE INDEX idx_audit_logs_entity      ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at  ON audit_logs (created_at);
