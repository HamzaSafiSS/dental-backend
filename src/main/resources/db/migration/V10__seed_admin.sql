-- ============================================================
-- V10: Seed initial admin account
-- ============================================================
-- Default admin credentials:
--   Email:    admin@dentalclinic.com
--   Password: Admin@1234
--
-- The password MUST be changed after first login.
-- The hash below is generated using pgcrypto's crypt() with
-- Blowfish (bf) cost 10, which produces a $2a$10$ BCrypt hash
-- fully compatible with Spring Security's BCryptPasswordEncoder.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'admin@dentalclinic.com',
    crypt('Admin@1234', gen_salt('bf', 10)),
    'System',
    'Admin',
    'ADMIN',
    TRUE,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE role = 'ADMIN'
);
