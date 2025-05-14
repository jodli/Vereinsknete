-- Drop all tables and triggers created in up.sql

DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS user_profile;

-- SQLite triggers are automatically dropped when their associated table is dropped
