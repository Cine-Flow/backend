-- Convert PostgreSQL native enum columns to VARCHAR so JPA @Enumerated(EnumType.STRING)
-- can bind values safely without explicit enum casts.

DO $$
BEGIN
    -- users.role: user_role -> varchar
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'role'
          AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN role TYPE VARCHAR(50)
            USING role::text;

        ALTER TABLE users
            ALTER COLUMN role SET DEFAULT 'ROLE_USER';
    END IF;

    -- films.type: film_type -> varchar
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'films'
          AND column_name = 'type'
          AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE films
            ALTER COLUMN type TYPE VARCHAR(20)
            USING type::text;
    END IF;

    -- user_subscriptions.status: subscription_status -> varchar
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'user_subscriptions'
          AND column_name = 'status'
          AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE user_subscriptions
            ALTER COLUMN status TYPE VARCHAR(20)
            USING status::text;
    END IF;
END $$;
