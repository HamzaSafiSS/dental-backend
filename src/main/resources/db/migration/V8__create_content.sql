-- ============================================================
-- V8: Content — Blog, FAQs, Reviews, Before/After cases
-- ============================================================

-- Blog categories
CREATE TABLE blog_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(200) NOT NULL,
    description     TEXT,
    display_order   INTEGER      NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_blog_categories_name UNIQUE (name),
    CONSTRAINT uq_blog_categories_slug UNIQUE (slug)
);

-- Blog posts
CREATE TABLE blog_posts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id         UUID,
    author_id           UUID,
    title               VARCHAR(500) NOT NULL,
    slug                VARCHAR(500) NOT NULL,
    summary             TEXT,
    content             TEXT,
    featured_image_url  VARCHAR(500),
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    published_at        TIMESTAMPTZ,
    meta_title          VARCHAR(200),
    meta_description    VARCHAR(500),
    tags                VARCHAR(500),
    view_count          INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_blog_posts_slug     UNIQUE (slug),
    CONSTRAINT fk_blog_posts_category FOREIGN KEY (category_id) REFERENCES blog_categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_posts_author   FOREIGN KEY (author_id)   REFERENCES users (id)            ON DELETE SET NULL,
    CONSTRAINT ck_blog_posts_status   CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_blog_posts_category_id ON blog_posts (category_id);
CREATE INDEX idx_blog_posts_status      ON blog_posts (status);
CREATE INDEX idx_blog_posts_published   ON blog_posts (published_at) WHERE status = 'PUBLISHED';

-- FAQ categories
CREATE TABLE faq_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(200) NOT NULL,
    description     TEXT,
    display_order   INTEGER      NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_faq_categories_name UNIQUE (name),
    CONSTRAINT uq_faq_categories_slug UNIQUE (slug)
);

-- FAQs
CREATE TABLE faqs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id     UUID,
    question        TEXT    NOT NULL,
    answer          TEXT    NOT NULL,
    display_order   INTEGER NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_faqs_category FOREIGN KEY (category_id) REFERENCES faq_categories (id) ON DELETE SET NULL
);

CREATE INDEX idx_faqs_category_id ON faqs (category_id);
CREATE INDEX idx_faqs_is_active   ON faqs (is_active);

-- Patient reviews
CREATE TABLE reviews (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID        NOT NULL,
    rating          SMALLINT    NOT NULL,
    comment         TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_response  TEXT,
    is_featured     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reviews_patient FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE,
    CONSTRAINT ck_reviews_rating  CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT ck_reviews_status  CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_reviews_patient_id ON reviews (patient_id);
CREATE INDEX idx_reviews_status     ON reviews (status);

-- Before/After treatment cases
CREATE TABLE before_after_cases (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id              UUID,
    patient_id              UUID,
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    before_image_url        VARCHAR(500) NOT NULL,
    after_image_url         VARCHAR(500) NOT NULL,
    has_patient_consent     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN      NOT NULL DEFAULT FALSE,
    display_order           INTEGER      NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_before_after_service FOREIGN KEY (service_id) REFERENCES services (id)  ON DELETE SET NULL,
    CONSTRAINT fk_before_after_patient FOREIGN KEY (patient_id) REFERENCES patients (id)  ON DELETE SET NULL
);

CREATE INDEX idx_before_after_service_id ON before_after_cases (service_id);
