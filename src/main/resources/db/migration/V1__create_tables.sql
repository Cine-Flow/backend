-- ============================================================
-- CineFlow Database Initialization Script (PostgreSQL)
-- Run as superuser: psql -U postgres -f init.sql
-- ============================================================

-- Create user if not exists (run as superuser)
DO $$ BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'myuser') THEN
    CREATE USER myuser WITH PASSWORD 'mypassword';
  END IF;
END $$;

-- Create database if not exists (must be run outside a transaction block)
-- Run manually if needed: CREATE DATABASE mydb OWNER myuser;

GRANT ALL PRIVILEGES ON DATABASE mydb TO myuser;

-- ========================
-- Enum types
-- ========================
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('ROLE_USER', 'ROLE_ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE film_type AS ENUM ('SINGLE', 'SERIES', 'LIVE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'EXPIRED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ========================
-- Table: users
-- ========================
CREATE TABLE IF NOT EXISTS users (
    id                 VARCHAR(36)   NOT NULL PRIMARY KEY,
    username           VARCHAR(255)  NOT NULL UNIQUE,
    email              VARCHAR(255)  NOT NULL UNIQUE,
    phone_number       VARCHAR(50)   UNIQUE,
    password           VARCHAR(255)  NOT NULL,
    full_name          VARCHAR(255),
    avatar_url         VARCHAR(500),
    role               user_role     NOT NULL DEFAULT 'ROLE_USER',
    reset_token        VARCHAR(255),
    reset_token_expiry TIMESTAMP(6),
    created_at         TIMESTAMP(6)  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP(6)  NOT NULL DEFAULT NOW()
);

-- ========================
-- Table: categories
-- ========================
CREATE TABLE IF NOT EXISTS categories (
    id          SERIAL        PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    description TEXT
);

-- ========================
-- Table: films
-- ========================
CREATE TABLE IF NOT EXISTS films (
    id            SERIAL        PRIMARY KEY,
    title         VARCHAR(255)  NOT NULL,
    description   TEXT,
    thumbnail_url VARCHAR(500),
    trailer_url   VARCHAR(500),
    release_year  INT,
    is_premium    BOOLEAN       NOT NULL DEFAULT FALSE,
    type          film_type     NOT NULL
);

-- ========================
-- Table: film_categories (junction)
-- ========================
CREATE TABLE IF NOT EXISTS film_categories (
    film_id     INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (film_id, category_id),
    CONSTRAINT fk_fc_film     FOREIGN KEY (film_id)     REFERENCES films(id)      ON DELETE CASCADE,
    CONSTRAINT fk_fc_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- ========================
-- Table: episodes
-- ========================
CREATE TABLE IF NOT EXISTS episodes (
    id             SERIAL        PRIMARY KEY,
    film_id        INT           NOT NULL,
    episode_number INT           NOT NULL,
    title          VARCHAR(255)  NOT NULL,
    video_url      VARCHAR(500),
    duration       INT,
    view_count     INT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_ep_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

-- ========================
-- Table: packages
-- ========================
CREATE TABLE IF NOT EXISTS packages (
    id            SERIAL          PRIMARY KEY,
    name          VARCHAR(255)    NOT NULL,
    price         DECIMAL(10, 2)  NOT NULL,
    duration_days INT             NOT NULL
);

-- ========================
-- Table: user_subscriptions
-- ========================
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id         SERIAL              PRIMARY KEY,
    user_id    VARCHAR(36)         NOT NULL,
    package_id INT                 NOT NULL,
    start_date TIMESTAMP           NOT NULL,
    end_date   TIMESTAMP           NOT NULL,
    status     subscription_status NOT NULL,
    CONSTRAINT fk_us_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_us_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE RESTRICT
);

-- ========================
-- Table: favorites
-- ========================
CREATE TABLE IF NOT EXISTS favorites (
    id       SERIAL       PRIMARY KEY,
    user_id  VARCHAR(36)  NOT NULL,
    film_id  INT          NOT NULL,
    added_at TIMESTAMP,
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_fav_film FOREIGN KEY (film_id) REFERENCES films(id)  ON DELETE CASCADE
);

-- ========================
-- Table: watch_history
-- ========================
CREATE TABLE IF NOT EXISTS watch_history (
    id                      SERIAL       PRIMARY KEY,
    user_id                 VARCHAR(36)  NOT NULL,
    episode_id              INT          NOT NULL,
    resume_position_seconds INT,
    last_watched_at         TIMESTAMP,
    CONSTRAINT fk_wh_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_wh_episode FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE
);

-- Grant privileges to myuser
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO myuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO myuser;

-- ============================================================
-- Mock Data
-- ============================================================

-- Categories
INSERT INTO categories (name, description) VALUES
('Hành động',           'Phim hành động kịch tính'),
('Lãng mạn',            'Phim tình cảm lãng mạn'),
('Kinh dị',             'Phim kinh dị, rùng rợn'),
('Hài hước',            'Phim hài giải trí'),
('Khoa học viễn tưởng', 'Phim sci-fi, viễn tưởng'),
('Hoạt hình',           'Phim hoạt hình cho mọi lứa tuổi'),
('Tài liệu',            'Phim tài liệu'),
('Thể thao',            'Phim thể thao')
ON CONFLICT DO NOTHING;

-- Subscription packages
INSERT INTO packages (name, price, duration_days) VALUES
('Cơ bản',     49000.00,   30),
('Tiêu chuẩn', 99000.00,   30),
('Cao cấp',   199000.00,   30),
('Năm',       999000.00,  365)
ON CONFLICT DO NOTHING;

-- Admin user (password: Admin@123)
INSERT INTO users (id, username, email, password, full_name, role) VALUES
('00000000-0000-0000-0000-000000000001', 'admin', 'admin@cineflow.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'CineFlow Admin', 'ROLE_ADMIN')
ON CONFLICT DO NOTHING;

-- ── SINGLE films (id 1–7) – dùng cho dailyMovies + banners ──────────────────
INSERT INTO films (title, description, thumbnail_url, trailer_url, release_year, is_premium, type) VALUES
('Avengers: Endgame',
 'Siêu anh hùng Marvel hội tụ để đánh bại Thanos và đảo ngược hậu quả của Infinity War.',
 'https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg',
 NULL, 2019, FALSE, 'SINGLE'),

('Spider-Man: No Way Home',
 'Peter Parker nhờ Doctor Strange xóa ký ức thế giới về danh tính của mình.',
 'https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg',
 NULL, 2021, FALSE, 'SINGLE'),

('Interstellar',
 'Một nhóm phi hành gia vượt qua lỗ sâu đục để tìm kiếm ngôi nhà mới cho nhân loại.',
 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
 NULL, 2014, TRUE, 'SINGLE'),

('The Dark Knight',
 'Batman đối mặt với Joker – kẻ muốn đẩy Gotham City vào hỗn loạn tuyệt đối.',
 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg',
 NULL, 2008, FALSE, 'SINGLE'),

('Inception',
 'Dom Cobb có khả năng xâm nhập vào giấc mơ để đánh cắp bí mật. Lần này anh phải cấy ghép một ý tưởng.',
 'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg',
 NULL, 2010, FALSE, 'SINGLE'),

('Top Gun: Maverick',
 'Pete Mitchell trở lại huấn luyện thế hệ phi công mới trong một nhiệm vụ bí mật nguy hiểm.',
 'https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg',
 NULL, 2022, FALSE, 'SINGLE'),

('Oppenheimer',
 'Câu chuyện về J. Robert Oppenheimer – cha đẻ của bom nguyên tử – và mặt trái của thiên tài.',
 'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg',
 NULL, 2023, TRUE, 'SINGLE');

-- ── SERIES films (id 8–15) – dùng cho hotSeries + newReleases ───────────────
INSERT INTO films (title, description, thumbnail_url, trailer_url, release_year, is_premium, type) VALUES
('Squid Game',
 'Những người mắc nợ tham gia trò chơi bí ẩn với giải thưởng 45,6 tỷ won nhưng đánh đổi bằng mạng sống.',
 'https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNLCjCSvE.jpg',
 NULL, 2021, TRUE, 'SERIES'),

('One Piece',
 'Monkey D. Luffy và băng hải tặc Mũ Rơm phiêu lưu khắp đại dương để tìm kho báu One Piece.',
 'https://image.tmdb.org/t/p/w500/fcRBORofZiH3GqNxE7VTrTkBmCM.jpg',
 NULL, 1999, FALSE, 'SERIES'),

('Breaking Bad',
 'Giáo viên hóa học Walter White chuyển thành trùm ma túy sau khi mắc bệnh ung thư phổi.',
 'https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L9pCfOacjizRGt.jpg',
 NULL, 2008, TRUE, 'SERIES'),

('Stranger Things',
 'Nhóm bạn nhỏ ở Hawkins khám phá thế giới ngược khi người bạn Will biến mất bí ẩn.',
 'https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg',
 NULL, 2016, FALSE, 'SERIES'),

('The Witcher',
 'Geralt of Rivia – thợ săn quái vật – lang thang qua thế giới nguy hiểm đầy phép thuật và chính trị.',
 'https://image.tmdb.org/t/p/w500/7vjaCdMw15FEbXyLQTVa04URsPm.jpg',
 NULL, 2019, FALSE, 'SERIES'),

('Money Heist',
 'Một tên tội phạm thiên tài tổ chức vụ cướp ngân hàng lớn nhất trong lịch sử cùng băng đảng của mình.',
 'https://image.tmdb.org/t/p/w500/reEMJA1uzscCbkpeRJeTT2bjqUp.jpg',
 NULL, 2017, TRUE, 'SERIES'),

('Dark',
 'Một vụ mất tích ở thị trấn nhỏ Winden hé lộ bí ẩn xuyên thời gian qua ba thế hệ.',
 'https://image.tmdb.org/t/p/w500/apbrbWs8M9lyOpJYU5WXrFndli6.jpg',
 NULL, 2017, FALSE, 'SERIES'),

('Naruto Shippuden',
 'Naruto Uzumaki tiếp tục hành trình trở thành Hokage và đối mặt với tổ chức Akatsuki nguy hiểm.',
 'https://image.tmdb.org/t/p/w500/xppe2DGOSkNHeKLFx76U5kFvzqZ.jpg',
 NULL, 2007, FALSE, 'SERIES');

-- ── LIVE films (id 16–20) – dùng cho sportEvents ─────────────────────────────
INSERT INTO films (title, description, thumbnail_url, trailer_url, release_year, is_premium, type) VALUES
('FIFA World Cup 2026 – Khai Mạc',
 'Lễ khai mạc FIFA World Cup 2026 tại Mỹ, Mexico và Canada – sự kiện bóng đá lớn nhất hành tinh.',
 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e3/2026_FIFA_World_Cup.svg/250px-2026_FIFA_World_Cup.svg.png',
 'https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 2026, TRUE, 'LIVE'),

('Champions League – Chung Kết',
 'Trận chung kết UEFA Champions League – đêm đỉnh cao của bóng đá châu Âu.',
 'https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/220px-UEFA_Champions_League_logo_2.svg.png',
 'https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 2026, TRUE, 'LIVE'),

('NBA Finals 2026 – Game 7',
 'Trận quyết định NBA Finals 2026 – ai sẽ nâng cao chiếc cúp Larry O''Brien?',
 'https://upload.wikimedia.org/wikipedia/en/thumb/0/03/National_Basketball_Association_logo.svg/200px-National_Basketball_Association_logo.svg.png',
 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 2026, FALSE, 'LIVE'),

('Wimbledon 2026 – Nam Đơn Chung Kết',
 'Chung kết đơn nam Wimbledon 2026 trên sân cỏ huyền thoại Centre Court.',
 'https://upload.wikimedia.org/wikipedia/en/thumb/9/9d/Wimbledon_2023_Logo.svg/220px-Wimbledon_2023_Logo.svg.png',
 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 2026, FALSE, 'LIVE'),

('Formula 1 – Monaco Grand Prix',
 'Chặng đua danh giá nhất lịch sử F1 quanh các đường phố Monte Carlo.',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/33/F1.svg/200px-F1.svg.png',
 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 2026, TRUE, 'LIVE');

-- ── Film categories ───────────────────────────────────────────────────────────
INSERT INTO film_categories (film_id, category_id) VALUES
(1,  1), (1,  5),   -- Avengers: Hành động, Sci-fi
(2,  1), (2,  5),   -- Spider-Man: Hành động, Sci-fi
(3,  5), (3,  7),   -- Interstellar: Sci-fi, Tài liệu
(4,  1), (4,  3),   -- Dark Knight: Hành động, Kinh dị
(5,  1), (5,  5),   -- Inception: Hành động, Sci-fi
(6,  1),            -- Top Gun: Hành động
(7,  7),            -- Oppenheimer: Tài liệu
(8,  1), (8,  3),   -- Squid Game: Hành động, Kinh dị
(9,  1), (9,  6),   -- One Piece: Hành động, Hoạt hình
(10, 1), (10, 3),   -- Breaking Bad: Hành động, Kinh dị
(11, 3), (11, 5),   -- Stranger Things: Kinh dị, Sci-fi
(12, 5),            -- The Witcher: Sci-fi
(13, 1), (13, 3),   -- Money Heist: Hành động, Kinh dị
(14, 3), (14, 5),   -- Dark: Kinh dị, Sci-fi
(15, 1), (15, 6),   -- Naruto: Hành động, Hoạt hình
(16, 8),            -- FIFA: Thể thao
(17, 8),            -- Champions League: Thể thao
(18, 8),            -- NBA: Thể thao
(19, 8),            -- Wimbledon: Thể thao
(20, 8)             -- F1: Thể thao
ON CONFLICT DO NOTHING;

-- ── Episodes for Squid Game (film_id = 8) ────────────────────────────────────
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(8, 1, 'Hồng Đăng Lục Đăng',            NULL, 3600, 12500000),
(8, 2, 'Địa ngục',                       NULL, 3480, 11200000),
(8, 3, 'Người đàn ông dù',               NULL, 3540, 10800000),
(8, 4, 'Người bắt đầu một trò chơi',     NULL, 3420,  9900000),
(8, 5, 'A Fair World',                   NULL, 3600,  9500000),
(8, 6, 'Gganbu',                         NULL, 3660,  9800000),
(8, 7, 'VIPS',                           NULL, 3600,  9100000),
(8, 8, 'Front Man',                      NULL, 3540,  8900000),
(8, 9, 'One Lucky Day',                  NULL, 3720,  8700000)
ON CONFLICT DO NOTHING;

-- Episodes for One Piece (film_id = 9)
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(9, 1, 'I''m Luffy! The Man Who''s Gonna Be King of the Pirates!', NULL, 1440, 5000000),
(9, 2, 'The Great Swordsman Appears! Pirate Hunter Roronoa Zoro!', NULL, 1440, 4800000),
(9, 3, 'Morgan Versus Luffy! Who''s This Beautiful Young Girl?',   NULL, 1440, 4600000),
(9, 4, 'Luffy''s Past! The Red-Haired Shanks Appears!',           NULL, 1440, 4400000),
(9, 5, 'Fear, Mysterious Power! Pirate Clown Captain Buggy!',      NULL, 1440, 4200000)
ON CONFLICT DO NOTHING;

-- Episodes for Breaking Bad (film_id = 10)
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(10, 1, 'Pilot',                        NULL, 3660, 8000000),
(10, 2, 'Cat''s in the Bag',            NULL, 3480, 7500000),
(10, 3, 'And the Bag''s in the River',  NULL, 3540, 7200000)
ON CONFLICT DO NOTHING;

-- Episodes for Stranger Things (film_id = 11)
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(11, 1, 'The Vanishing of Will Byers',  NULL, 3540, 9000000),
(11, 2, 'The Weirdo on Maple Street',   NULL, 3420, 8500000),
(11, 3, 'Holly, Jolly',                 NULL, 3600, 8200000)
ON CONFLICT DO NOTHING;

-- Episodes for Naruto Shippuden (film_id = 15)
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(15, 1, 'Homecoming',                   NULL, 1440, 6000000),
(15, 2, 'The Akatsuki Makes Its Move',  NULL, 1440, 5800000),
(15, 3, 'The Results of Training',      NULL, 1440, 5600000)
ON CONFLICT DO NOTHING;

-- Single film episodes (film_id 1–7, each 1 episode)
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(1, 1, 'Avengers: Endgame (Full Movie)',        NULL, 10860, 0),
(2, 1, 'Spider-Man: No Way Home (Full Movie)',  NULL,  9780, 0),
(3, 1, 'Interstellar (Full Movie)',             NULL, 10200, 0),
(4, 1, 'The Dark Knight (Full Movie)',          NULL,  9120, 0),
(5, 1, 'Inception (Full Movie)',                NULL,  8880, 0),
(6, 1, 'Top Gun: Maverick (Full Movie)',        NULL,  8040, 0),
(7, 1, 'Oppenheimer (Full Movie)',              NULL, 11040, 0)
ON CONFLICT DO NOTHING;
