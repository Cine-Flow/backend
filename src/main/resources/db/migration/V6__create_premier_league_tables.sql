CREATE TABLE football_teams (
    id       SERIAL        PRIMARY KEY,
    code     VARCHAR(10)   NOT NULL UNIQUE,
    name     VARCHAR(255)  NOT NULL,
    logo_url VARCHAR(1000)
);

CREATE TABLE football_matches (
    id           SERIAL        PRIMARY KEY,
    home_team_id INT           NOT NULL,
    away_team_id INT           NOT NULL,
    kickoff_at   TIMESTAMP     NOT NULL,
    round        VARCHAR(100)  NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    home_score   INT,
    away_score   INT,
    banner_url   VARCHAR(1000),
    highlight_url VARCHAR(1000),
    CONSTRAINT fk_match_home_team FOREIGN KEY (home_team_id) REFERENCES football_teams(id),
    CONSTRAINT fk_match_away_team FOREIGN KEY (away_team_id) REFERENCES football_teams(id),
    CONSTRAINT chk_match_different_teams CHECK (home_team_id <> away_team_id),
    CONSTRAINT chk_match_status CHECK (status IN ('SCHEDULED', 'LIVE', 'FINISHED'))
);

CREATE TABLE football_standings (
    id              SERIAL        PRIMARY KEY,
    team_id         INT           NOT NULL,
    season          VARCHAR(20)   NOT NULL,
    rank            INT           NOT NULL,
    played          INT           NOT NULL DEFAULT 0,
    won             INT           NOT NULL DEFAULT 0,
    drawn           INT           NOT NULL DEFAULT 0,
    lost            INT           NOT NULL DEFAULT 0,
    goal_difference INT           NOT NULL DEFAULT 0,
    points          INT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_standing_team FOREIGN KEY (team_id) REFERENCES football_teams(id),
    CONSTRAINT uk_standing_season_team UNIQUE (season, team_id),
    CONSTRAINT uk_standing_season_rank UNIQUE (season, rank)
);

CREATE TABLE football_contents (
    id            SERIAL        PRIMARY KEY,
    title         VARCHAR(255)  NOT NULL,
    thumbnail_url VARCHAR(1000),
    video_url     VARCHAR(1000),
    content_type  VARCHAR(20)   NOT NULL,
    badge         VARCHAR(50),
    published_at  TIMESTAMP     NOT NULL,
    CONSTRAINT chk_content_type CHECK (content_type IN ('BANNER', 'HIGHLIGHT', 'NEWS'))
);

CREATE INDEX idx_football_matches_status_kickoff
    ON football_matches (status, kickoff_at);

CREATE INDEX idx_football_standings_season_rank
    ON football_standings (season, rank);

CREATE INDEX idx_football_contents_type_published
    ON football_contents (content_type, published_at DESC);

INSERT INTO football_teams (code, name, logo_url) VALUES
('ARS', 'Arsenal', 'https://resources.premierleague.com/premierleague/badges/50/t3.png'),
('AVL', 'Aston Villa', 'https://resources.premierleague.com/premierleague/badges/50/t7.png'),
('BOU', 'Bournemouth', 'https://resources.premierleague.com/premierleague/badges/50/t91.png'),
('BRE', 'Brentford', 'https://resources.premierleague.com/premierleague/badges/50/t94.png'),
('BHA', 'Brighton', 'https://resources.premierleague.com/premierleague/badges/50/t36.png'),
('BUR', 'Burnley', 'https://resources.premierleague.com/premierleague/badges/50/t90.png'),
('CHE', 'Chelsea', 'https://resources.premierleague.com/premierleague/badges/50/t8.png'),
('EVE', 'Everton', 'https://resources.premierleague.com/premierleague/badges/50/t11.png'),
('FUL', 'Fulham', 'https://resources.premierleague.com/premierleague/badges/50/t54.png'),
('LEE', 'Leeds United', 'https://resources.premierleague.com/premierleague/badges/50/t2.png'),
('LIV', 'Liverpool', 'https://resources.premierleague.com/premierleague/badges/50/t14.png'),
('MCI', 'Manchester City', 'https://resources.premierleague.com/premierleague/badges/50/t43.png'),
('MUN', 'Manchester United', 'https://resources.premierleague.com/premierleague/badges/50/t1.png'),
('NEW', 'Newcastle United', 'https://resources.premierleague.com/premierleague/badges/50/t4.png'),
('NFO', 'Nottingham Forest', 'https://resources.premierleague.com/premierleague/badges/50/t17.png'),
('SUN', 'Sunderland', 'https://resources.premierleague.com/premierleague/badges/50/t56.png'),
('TOT', 'Tottenham Hotspur', 'https://resources.premierleague.com/premierleague/badges/50/t6.png'),
('WHU', 'West Ham United', 'https://resources.premierleague.com/premierleague/badges/50/t21.png'),
('WOL', 'Wolverhampton Wanderers', 'https://resources.premierleague.com/premierleague/badges/50/t39.png');

INSERT INTO football_matches (
    home_team_id, away_team_id, kickoff_at, round, status, home_score, away_score, banner_url, highlight_url
) VALUES
((SELECT id FROM football_teams WHERE code = 'LEE'), (SELECT id FROM football_teams WHERE code = 'BRE'),
 '2026-04-11 18:30:00', 'Vòng 32', 'LIVE', 1, 0,
 'https://images.unsplash.com/photo-1522778119026-d647f0596c20?auto=format&fit=crop&w=1200&q=80', NULL),
((SELECT id FROM football_teams WHERE code = 'WHU'), (SELECT id FROM football_teams WHERE code = 'WOL'),
 '2026-04-11 02:00:00', 'Vòng 32', 'SCHEDULED', NULL, NULL, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'ARS'), (SELECT id FROM football_teams WHERE code = 'BOU'),
 '2026-04-11 18:30:00', 'Vòng 32', 'SCHEDULED', NULL, NULL, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'BRE'), (SELECT id FROM football_teams WHERE code = 'EVE'),
 '2026-04-11 21:00:00', 'Vòng 32', 'SCHEDULED', NULL, NULL, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'BUR'), (SELECT id FROM football_teams WHERE code = 'BHA'),
 '2026-04-11 21:00:00', 'Vòng 32', 'SCHEDULED', NULL, NULL, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'LIV'), (SELECT id FROM football_teams WHERE code = 'FUL'),
 '2026-04-11 23:30:00', 'Vòng 32', 'SCHEDULED', NULL, NULL, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'AVL'), (SELECT id FROM football_teams WHERE code = 'WHU'),
 '2026-03-22 18:30:00', 'Vòng 31', 'FINISHED', 2, 0, NULL,
 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4'),
((SELECT id FROM football_teams WHERE code = 'TOT'), (SELECT id FROM football_teams WHERE code = 'NFO'),
 '2026-03-22 20:00:00', 'Vòng 31', 'FINISHED', 0, 3, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'NEW'), (SELECT id FROM football_teams WHERE code = 'SUN'),
 '2026-03-22 21:00:00', 'Vòng 31', 'FINISHED', 1, 2, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'LEE'), (SELECT id FROM football_teams WHERE code = 'BRE'),
 '2026-03-22 22:00:00', 'Vòng 31', 'FINISHED', 0, 0, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'EVE'), (SELECT id FROM football_teams WHERE code = 'CHE'),
 '2026-03-22 23:30:00', 'Vòng 31', 'FINISHED', 3, 0, NULL, NULL);

INSERT INTO football_standings (team_id, season, rank, played, won, drawn, lost, goal_difference, points) VALUES
((SELECT id FROM football_teams WHERE code = 'ARS'), '2025/26', 1, 31, 21, 7, 3, 39, 70),
((SELECT id FROM football_teams WHERE code = 'MCI'), '2025/26', 2, 30, 18, 7, 5, 32, 61),
((SELECT id FROM football_teams WHERE code = 'MUN'), '2025/26', 3, 31, 15, 10, 6, 13, 55),
((SELECT id FROM football_teams WHERE code = 'AVL'), '2025/26', 4, 31, 16, 6, 9, 5, 54),
((SELECT id FROM football_teams WHERE code = 'LIV'), '2025/26', 5, 31, 14, 7, 10, 8, 49),
((SELECT id FROM football_teams WHERE code = 'CHE'), '2025/26', 6, 31, 13, 9, 9, 15, 48);

INSERT INTO football_contents (title, thumbnail_url, video_url, content_type, badge, published_at) VALUES
('Leeds vs Brentford', 'https://images.unsplash.com/photo-1522778119026-d647f0596c20?auto=format&fit=crop&w=1200&q=80', NULL, 'BANNER', 'LIVE', '2026-04-11 18:30:00'),
('Aston Villa vs West Ham', 'https://images.unsplash.com/photo-1579952363873-27f3bade9f55?auto=format&fit=crop&w=900&q=80', 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 'HIGHLIGHT', 'MỚI', '2026-03-23 08:00:00'),
('Arsenal vs Liverpool', 'https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?auto=format&fit=crop&w=900&q=80', 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 'HIGHLIGHT', 'HOT', '2026-03-22 08:00:00'),
('Tin nóng Ngoại hạng', 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=900&q=80', NULL, 'NEWS', 'MỚI', '2026-03-23 10:00:00'),
('Review Vòng 31', 'https://images.unsplash.com/photo-1553778263-73a83bab9b0c?auto=format&fit=crop&w=900&q=80', NULL, 'NEWS', NULL, '2026-03-22 10:00:00');
