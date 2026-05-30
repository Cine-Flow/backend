INSERT INTO football_teams (code, name, logo_url) VALUES
('CRY', 'Crystal Palace', 'https://resources.premierleague.com/premierleague/badges/50/t31.png')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    logo_url = EXCLUDED.logo_url;

INSERT INTO football_standings (
    team_id, season, rank, played, won, drawn, lost, goal_difference, points
) VALUES
((SELECT id FROM football_teams WHERE code = 'ARS'), '2025/26', 1, 31, 21, 7, 3, 39, 70),
((SELECT id FROM football_teams WHERE code = 'MCI'), '2025/26', 2, 30, 18, 7, 5, 32, 61),
((SELECT id FROM football_teams WHERE code = 'MUN'), '2025/26', 3, 31, 15, 10, 6, 13, 55),
((SELECT id FROM football_teams WHERE code = 'AVL'), '2025/26', 4, 31, 16, 6, 9, 5, 54),
((SELECT id FROM football_teams WHERE code = 'LIV'), '2025/26', 5, 31, 14, 7, 10, 8, 49),
((SELECT id FROM football_teams WHERE code = 'CHE'), '2025/26', 6, 31, 13, 9, 9, 15, 48),
((SELECT id FROM football_teams WHERE code = 'NEW'), '2025/26', 7, 31, 13, 8, 10, 10, 47),
((SELECT id FROM football_teams WHERE code = 'TOT'), '2025/26', 8, 31, 12, 8, 11, 7, 44),
((SELECT id FROM football_teams WHERE code = 'BHA'), '2025/26', 9, 31, 11, 9, 11, 5, 42),
((SELECT id FROM football_teams WHERE code = 'BRE'), '2025/26', 10, 31, 11, 8, 12, 2, 41),
((SELECT id FROM football_teams WHERE code = 'CRY'), '2025/26', 11, 30, 10, 10, 10, 1, 40),
((SELECT id FROM football_teams WHERE code = 'FUL'), '2025/26', 12, 31, 10, 8, 13, -3, 38),
((SELECT id FROM football_teams WHERE code = 'EVE'), '2025/26', 13, 31, 9, 9, 13, -7, 36),
((SELECT id FROM football_teams WHERE code = 'BOU'), '2025/26', 14, 31, 9, 8, 14, -9, 35),
((SELECT id FROM football_teams WHERE code = 'NFO'), '2025/26', 15, 31, 8, 8, 15, -12, 32),
((SELECT id FROM football_teams WHERE code = 'WHU'), '2025/26', 16, 31, 7, 8, 16, -15, 29),
((SELECT id FROM football_teams WHERE code = 'LEE'), '2025/26', 17, 31, 7, 7, 17, -18, 28),
((SELECT id FROM football_teams WHERE code = 'WOL'), '2025/26', 18, 31, 6, 7, 18, -22, 25),
((SELECT id FROM football_teams WHERE code = 'BUR'), '2025/26', 19, 31, 5, 7, 19, -27, 22),
((SELECT id FROM football_teams WHERE code = 'SUN'), '2025/26', 20, 31, 4, 8, 19, -31, 20)
ON CONFLICT (season, team_id) DO UPDATE SET
    rank = EXCLUDED.rank,
    played = EXCLUDED.played,
    won = EXCLUDED.won,
    drawn = EXCLUDED.drawn,
    lost = EXCLUDED.lost,
    goal_difference = EXCLUDED.goal_difference,
    points = EXCLUDED.points;

INSERT INTO football_matches (
    home_team_id, away_team_id, kickoff_at, round, status,
    home_score, away_score, banner_url, highlight_url
) VALUES
((SELECT id FROM football_teams WHERE code = 'MCI'), (SELECT id FROM football_teams WHERE code = 'CRY'),
 '2026-04-12 20:00:00', 'Round 32', 'SCHEDULED', NULL, NULL, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'CHE'), (SELECT id FROM football_teams WHERE code = 'NEW'),
 '2026-04-12 22:30:00', 'Round 32', 'SCHEDULED', NULL, NULL, NULL, NULL),
((SELECT id FROM football_teams WHERE code = 'ARS'), (SELECT id FROM football_teams WHERE code = 'LIV'),
 '2026-03-21 23:30:00', 'Round 31', 'FINISHED', 2, 1, NULL,
 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4');

INSERT INTO football_contents (
    title, thumbnail_url, video_url, content_type, badge, published_at
) VALUES
('Manchester City vs Crystal Palace',
 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1200&q=80',
 NULL, 'BANNER', 'UPCOMING', '2026-04-12 19:00:00'),
('Arsenal vs Liverpool highlights',
 'https://images.unsplash.com/photo-1526232761682-d26e03ac148e?auto=format&fit=crop&w=900&q=80',
 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
 'HIGHLIGHT', 'HOT', '2026-03-22 09:00:00'),
('Premier League round 32 preview',
 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=900&q=80',
 NULL, 'NEWS', 'NEW', '2026-04-10 10:00:00');
