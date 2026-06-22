-- V18__add_user_enabled_column.sql
-- Adds an enabled flag to users so admins can block/unblock accounts from logging in

ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
