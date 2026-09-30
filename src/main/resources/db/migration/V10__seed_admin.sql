CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'safihamza.com',
    crypt('safi1234', gen_salt('bf', 10)),
    'Hamza',
    'Safi',
    'ADMIN',
    TRUE,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE role = 'ADMIN'
);
