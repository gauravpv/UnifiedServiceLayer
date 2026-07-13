-- =============================================================================
-- PostgreSQL
-- Optional: create database (run as a superuser / postgres role).
-- Skip this if the DB already exists.
-- =============================================================================

CREATE DATABASE usl_bre
    WITH OWNER = CURRENT_USER
         ENCODING = 'UTF8'
         TEMPLATE = template0;
