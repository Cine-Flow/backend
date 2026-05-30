DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_favorites_user_film'
    ) THEN
        ALTER TABLE favorites
            ADD CONSTRAINT uk_favorites_user_film UNIQUE (user_id, film_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_favorites_user_id
    ON favorites (user_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_watch_history_user_episode'
    ) THEN
        ALTER TABLE watch_history
            ADD CONSTRAINT uk_watch_history_user_episode UNIQUE (user_id, episode_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_watch_history_user_id
    ON watch_history (user_id);
