-- Fix admin password hash: re-hash with BCrypt strength 14 to match BCryptPasswordEncoder(14) in AppConfig
-- Password: Admin@123
UPDATE users
SET password = '$2b$14$lam43B2vCqCmheppyRObM.gx/FjCbvXPeRZYYgoq8e6AwPL7C8fbi'
WHERE id = '00000000-0000-0000-0000-000000000001';
