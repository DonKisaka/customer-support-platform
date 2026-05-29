-- Create databases
CREATE DATABASE billing_db;
CREATE DATABASE techsupport_db;

-- Enable pgvector extension in techsupport_db
\c techsupport_db;
CREATE EXTENSION IF NOT EXISTS vector;
