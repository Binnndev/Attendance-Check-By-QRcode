-- =====================================================================
-- V3__seed_demo.sql (DEV ONLY)
-- =====================================================================

SET @owner_id := UUID_TO_BIN(UUID(), 1);
SET @group_id := UUID_TO_BIN(UUID(), 1);
SET @s1 := UUID_TO_BIN(UUID(), 1);
SET @s2 := UUID_TO_BIN(UUID(), 1);
SET @s3 := UUID_TO_BIN(UUID(), 1);

INSERT INTO users (id, platform_role, email, password_hash, full_name, user_code, status)
VALUES (@owner_id, 'USER', 'owner@demo.local', '$2b$10$demo_hash_not_for_prod', 'Owner Demo', 'OWN001', 'ACTIVE');
