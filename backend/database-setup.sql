-- PostgreSQL Database Setup for IELTS Speaking App
-- Run this script as a PostgreSQL superuser (usually 'postgres')

-- Create database
CREATE DATABASE ielts_speaking_app;

-- Connect to the database (run this in psql after creating the database)
\c ielts_speaking_app;

-- Create user (optional - you can use the default postgres user)
-- CREATE USER ielts_user WITH PASSWORD 'your_password';
-- GRANT ALL PRIVILEGES ON DATABASE ielts_speaking_app TO ielts_user;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- The tables will be created automatically by Hibernate/JPA when the application starts
-- with ddl-auto: update

-- To verify the setup, you can run:
-- \dt  -- List all tables
-- \d+ table_name  -- Describe table structure
