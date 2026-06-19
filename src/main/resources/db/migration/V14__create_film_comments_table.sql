CREATE TABLE IF NOT EXISTS film_comments (
    id          SERIAL         PRIMARY KEY,
    film_id     INT            NOT NULL,
    user_id     VARCHAR(36)    NOT NULL,
    content     TEXT           NOT NULL,
    created_at  TIMESTAMP(6)   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_film_comments_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    CONSTRAINT fk_film_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_film_comments_film_id
    ON film_comments (film_id);

CREATE INDEX IF NOT EXISTS idx_film_comments_film_created
    ON film_comments (film_id, created_at DESC);
