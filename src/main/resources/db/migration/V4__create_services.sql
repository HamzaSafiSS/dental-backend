-- ============================================================
-- V4: Service categories and dental services
-- ============================================================

-- Service categories (General Dentistry, Cosmetic, Orthodontics, etc.)
CREATE TABLE service_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(200) NOT NULL,
    description     TEXT,
    icon_url        VARCHAR(500),
    display_order   INTEGER      NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_service_categories_name UNIQUE (name),
    CONSTRAINT uq_service_categories_slug UNIQUE (slug)
);

-- Dental services offered by the clinic
CREATE TABLE services (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id                 UUID,
    name                        VARCHAR(200)   NOT NULL,
    slug                        VARCHAR(200)   NOT NULL,
    short_description           VARCHAR(500),
    full_description            TEXT,
    duration_minutes            INTEGER        NOT NULL DEFAULT 30,
    required_payment_amount     DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    image_url                   VARCHAR(500),
    is_active                   BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_services_slug     UNIQUE (slug),
    CONSTRAINT fk_services_category FOREIGN KEY (category_id) REFERENCES service_categories (id) ON DELETE SET NULL,
    CONSTRAINT ck_services_duration CHECK (duration_minutes > 0),
    CONSTRAINT ck_services_amount   CHECK (required_payment_amount >= 0)
);

CREATE INDEX idx_services_category_id ON services (category_id);
CREATE INDEX idx_services_is_active   ON services (is_active);
