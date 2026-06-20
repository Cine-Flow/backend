-- Update admin user's password to "123" (BCrypt strength 14 hash)
UPDATE users
SET password = '$2b$14$8dbR6uskFERVIM6HAVgSEOa/5G6E0Gj7T0hyHuw4u0JURrv.aUB5W'
WHERE username = 'admin' OR id = '00000000-0000-0000-0000-000000000001';

-- Insert default user account: user / 123
INSERT INTO users (id, username, email, password, full_name, role)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'user',
    'user@cineflow.com',
    '$2b$14$8dbR6uskFERVIM6HAVgSEOa/5G6E0Gj7T0hyHuw4u0JURrv.aUB5W',
    'CineFlow User',
    'ROLE_USER'
)
ON CONFLICT (username) DO UPDATE
SET password = EXCLUDED.password,
    email = EXCLUDED.email;
