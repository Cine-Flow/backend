DROP TABLE IF EXISTS football_standings;
DROP TABLE IF EXISTS football_matches;
DROP TABLE IF EXISTS football_contents;
DROP TABLE IF EXISTS football_teams;

ALTER TABLE films
    ADD COLUMN IF NOT EXISTS badge VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_films_type_badge
    ON films (type, badge);
