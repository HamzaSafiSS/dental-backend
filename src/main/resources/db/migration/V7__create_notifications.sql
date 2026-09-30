-- ============================================================
-- V7: Notifications
-- ============================================================

CREATE TABLE notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL,
    type                VARCHAR(50)  NOT NULL,
    channel             VARCHAR(20)  NOT NULL,
    title               VARCHAR(255),
    message             TEXT         NOT NULL,
    recipient_contact   VARCHAR(255),
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    sent_at             TIMESTAMPTZ,
    error_message       TEXT,
    reference_type      VARCHAR(50),
    reference_id        UUID,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_notifications_channel CHECK (channel IN ('SMS', 'EMAIL')),
    CONSTRAINT ck_notifications_status  CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_notifications_user_id   ON notifications (user_id);
CREATE INDEX idx_notifications_type      ON notifications (type);
CREATE INDEX idx_notifications_status    ON notifications (status);
CREATE INDEX idx_notifications_reference ON notifications (reference_type, reference_id);
