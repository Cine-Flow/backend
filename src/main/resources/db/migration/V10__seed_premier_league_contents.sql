INSERT INTO films (title, description, thumbnail_url, trailer_url, release_year, is_premium, type, badge)
SELECT seed.title, seed.description, seed.thumbnail_url, seed.trailer_url, 2026, FALSE, 'SPORTS', seed.badge
FROM (VALUES
    ('Premier League Matchday 38', 'Follow the final matchday of the Premier League season.',
     'https://images.unsplash.com/photo-1522778119026-d647f0596c20?auto=format&fit=crop&w=1200&q=80',
     NULL, 'BANNER'),
    ('Title Race: Man City vs Arsenal', 'A decisive match in the Premier League title race.',
     'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1200&q=80',
     NULL, 'BANNER'),
    ('Top Goals of Matchday 37', 'Watch the best goals from the latest Premier League matches.',
     'https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?auto=format&fit=crop&w=900&q=80',
     'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 'HIGHLIGHT'),
    ('Arsenal 3-1 Chelsea Highlights', 'Highlights from Arsenal versus Chelsea.',
     'https://images.unsplash.com/photo-1526232761682-d26e03ac148e?auto=format&fit=crop&w=900&q=80',
     'https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 'HIGHLIGHT'),
    ('Liverpool 2-2 Tottenham Highlights', 'Highlights from Liverpool versus Tottenham.',
     'https://images.unsplash.com/photo-1553778263-73a83bab9b0c?auto=format&fit=crop&w=900&q=80',
     'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 'HIGHLIGHT'),
    ('Premier League title race reaches final day', 'The championship will be decided on the final matchday.',
     'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=900&q=80',
     NULL, 'NEWS'),
    ('Five matches to watch this weekend', 'A preview of the key fixtures in Matchday 38.',
     'https://images.unsplash.com/photo-1518604666860-9ed391f76460?auto=format&fit=crop&w=900&q=80',
     NULL, 'NEWS'),
    ('Golden Boot race enters its final round', 'The league top scorers prepare for one last push.',
     'https://images.unsplash.com/photo-1517466787929-bc90951d0974?auto=format&fit=crop&w=900&q=80',
     NULL, 'NEWS')
) AS seed(title, description, thumbnail_url, trailer_url, badge)
WHERE NOT EXISTS (
    SELECT 1
    FROM films
    WHERE films.title = seed.title
      AND films.type = 'SPORTS'
);
