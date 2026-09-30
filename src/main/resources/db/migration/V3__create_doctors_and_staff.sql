CREATE TABLE doctors (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL,
    specialization      VARCHAR(200),
    qualification       VARCHAR(500),
    experience_years    INTEGER,
    bio                 TEXT,
    license_number      VARCHAR(100),
    profile_image_url   VARCHAR(500),
    consultation_fee    DECIMAL(10,2),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_doctors_user_id        UNIQUE (user_id),
    CONSTRAINT fk_doctors_user           FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Doctor weekly availability (multiple time blocks per day allowed)
CREATE TABLE doctor_availability (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id       UUID        NOT NULL,
    day_of_week     VARCHAR(10) NOT NULL,
    start_time      TIME        NOT NULL,
    end_time        TIME        NOT NULL,
    is_available    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_availability_doctor    FOREIGN KEY (doctor_id) REFERENCES doctors (id) ON DELETE CASCADE,
    CONSTRAINT uq_availability_slot      UNIQUE (doctor_id, day_of_week, start_time),
    CONSTRAINT ck_availability_day       CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT ck_availability_time      CHECK (start_time < end_time)
);

CREATE INDEX idx_availability_doctor_id ON doctor_availability (doctor_id);

-- Doctor time-off periods
CREATE TABLE doctor_time_off (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id   UUID         NOT NULL,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    reason      VARCHAR(500),
    is_approved BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_time_off_doctor   FOREIGN KEY (doctor_id) REFERENCES doctors (id) ON DELETE CASCADE,
    CONSTRAINT ck_time_off_dates    CHECK (start_date <= end_date)
);

CREATE INDEX idx_time_off_doctor_id  ON doctor_time_off (doctor_id);
CREATE INDEX idx_time_off_dates      ON doctor_time_off (start_date, end_date);

-- Staff profiles for receptionists (linked 1:1 to users)
CREATE TABLE staff_profiles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL,
    position            VARCHAR(100),
    department          VARCHAR(100),
    profile_image_url   VARCHAR(500),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_staff_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_staff_profiles_user    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
