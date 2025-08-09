-- PostgreSQL Manual Setup Script for IELTS Speaking App
-- Run this script as the postgres superuser

-- Step 1: Create the user (if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ytms_user') THEN
        CREATE USER ytms_user WITH PASSWORD 'ytms_password';
    ELSE
        RAISE NOTICE 'User ytms_user already exists';
    END IF;
END
$$;

-- Step 2: Create the database (if it doesn't exist)
SELECT 'CREATE DATABASE ielts_speaking_app OWNER ytms_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ielts_speaking_app')\gexec

-- Step 3: Grant privileges to the user
GRANT ALL PRIVILEGES ON DATABASE ielts_speaking_app TO ytms_user;

-- Step 4: Connect to the new database and grant schema privileges
\c ielts_speaking_app;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO ytms_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ytms_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ytms_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO ytms_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ytms_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ytms_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO ytms_user;

-- Step 5: Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Step 6: Verify setup
\du ytms_user
\l ielts_speaking_app
