-- ============================================================
-- CineFlow Database Initialization Script
-- ============================================================

CREATE DATABASE IF NOT EXISTS cineflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cineflow;

-- ========================
-- Table: users
-- ========================
CREATE TABLE IF NOT EXISTS users (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone_number  VARCHAR(50)  UNIQUE,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    avatar_url    VARCHAR(500),
    role          ENUM('ROLE_USER', 'ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    reset_token   VARCHAR(255),
    reset_token_expiry DATETIME(6),
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- ========================
-- Table: categories
-- ========================
CREATE TABLE IF NOT EXISTS categories (
    id          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

-- ========================
-- Table: films
-- ========================
CREATE TABLE IF NOT EXISTS films (
    id            INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    thumbnail_url VARCHAR(500),
    trailer_url   VARCHAR(500),
    release_year  INT,
    is_premium    TINYINT(1)   NOT NULL DEFAULT 0,
    type          ENUM('SINGLE', 'SERIES', 'LIVE') NOT NULL
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
    id             INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    film_id        INT          NOT NULL,
    episode_number INT          NOT NULL,
    title          VARCHAR(255) NOT NULL,
    video_url      VARCHAR(500),
    duration       INT,
    view_count     INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_ep_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

-- ========================
-- Table: packages
-- ========================
CREATE TABLE IF NOT EXISTS packages (
    id            INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255)   NOT NULL,
    price         DECIMAL(10, 2) NOT NULL,
    duration_days INT            NOT NULL
);

-- ========================
-- Table: user_subscriptions
-- ========================
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id         INT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL,
    package_id INT      NOT NULL,
    start_date DATETIME NOT NULL,
    end_date   DATETIME NOT NULL,
    status     ENUM('ACTIVE', 'EXPIRED') NOT NULL,
    CONSTRAINT fk_us_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_us_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE RESTRICT
);

-- ========================
-- Table: favorites
-- ========================
CREATE TABLE IF NOT EXISTS favorites (
    id       INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id  VARCHAR(36) NOT NULL,
    film_id  INT         NOT NULL,
    added_at DATETIME,
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_fav_film FOREIGN KEY (film_id) REFERENCES films(id)  ON DELETE CASCADE
);

-- ========================
-- Table: watch_history
-- ========================
CREATE TABLE IF NOT EXISTS watch_history (
    id                      INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                 VARCHAR(36) NOT NULL,
    episode_id              INT         NOT NULL,
    resume_position_seconds INT,
    last_watched_at         DATETIME,
    CONSTRAINT fk_wh_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_wh_episode FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE
);

-- ============================================================
-- Sample Data
-- ============================================================

-- Categories
INSERT INTO categories (name, description) VALUES
('Hành động',   'Phim hành động kịch tính'),
('Lãng mạn',    'Phim tình cảm lãng mạn'),
('Kinh dị',     'Phim kinh dị, rùng rợn'),
('Hài hước',    'Phim hài giải trí'),
('Khoa học viễn tưởng', 'Phim sci-fi, viễn tưởng'),
('Hoạt hình',   'Phim hoạt hình cho mọi lứa tuổi'),
('Tài liệu',    'Phim tài liệu'),
('Thể thao',    'Phim thể thao');

-- Subscription packages
INSERT INTO packages (name, price, duration_days) VALUES
('Cơ bản',   49000.00,  30),
('Tiêu chuẩn', 99000.00, 30),
('Cao cấp',  199000.00, 30),
('Năm',      999000.00, 365);

-- Admin user (password: Admin@123 – replace hash with actual BCrypt hash)
INSERT INTO users (id, username, email, password, full_name, role) VALUES
('00000000-0000-0000-0000-000000000001', 'admin', 'admin@cineflow.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'CineFlow Admin', 'ROLE_ADMIN');

-- Sample films
INSERT INTO films (title, description, thumbnail_url, trailer_url, release_year, is_premium, type) VALUES
('Avengers: Endgame', 'Siêu anh hùng Marvel hội tụ để đánh bại Thanos.', NULL, NULL, 2019, 0, 'SINGLE'),
('Squid Game',        'Trò chơi sinh tử kiếm tiền thưởng khổng lồ.',      NULL, NULL, 2021, 1, 'SERIES'),
('One Piece',         'Hành trình tìm kho báu của băng hải tặc Mũ Rơm.',  NULL, NULL, 1999, 0, 'SERIES');

-- Film categories
INSERT INTO film_categories (film_id, category_id) VALUES
(1, 1), -- Avengers: Hành động
(1, 5), -- Avengers: Khoa học viễn tưởng
(2, 1), -- Squid Game: Hành động
(2, 3), -- Squid Game: Kinh dị
(3, 1), -- One Piece: Hành động
(3, 6); -- One Piece: Hoạt hình

-- Episodes for Squid Game
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(2, 1, 'Hồng Đăng Lục Đăng',     NULL, 3600, 0),
(2, 2, 'Địa ngục',                NULL, 3480, 0),
(2, 3, 'Người đàn ông dù',        NULL, 3540, 0),
(2, 4, 'Người bắt đầu một trò chơi', NULL, 3420, 0),
(2, 5, 'A Fair World',            NULL, 3600, 0),
(2, 6, 'Gganbu',                  NULL, 3660, 0),
(2, 7, 'VIPS',                    NULL, 3600, 0),
(2, 8, 'Front Man',               NULL, 3540, 0),
(2, 9, 'One Lucky Day',           NULL, 3720, 0);

-- Episode for Avengers (single film – treated as 1 episode)
INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count) VALUES
(1, 1, 'Avengers: Endgame (Full Movie)', NULL, 10800, 0);
