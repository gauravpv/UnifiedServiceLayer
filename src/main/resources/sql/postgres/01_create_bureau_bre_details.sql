-- =============================================================================
-- PostgreSQL
-- Table: bureau_bre_details
-- Purpose: Cache + audit log for downstream Bureau (BRE) service responses.
-- Lookup key:         REQUEST_HASH (SHA-256 hex of the canonicalized `data` JSON).
-- Staleness anchor:   RESPONSE_TIMESTAMP (compared against now - 30 days).
-- Type discriminator: BT_ID = 'BUREAU' for every row in this table.
-- =============================================================================

CREATE TABLE IF NOT EXISTS bureau_bre_details (
    ID                 BIGSERIAL,
    REQUEST_JSON       BYTEA,
    REQUEST_HASH       VARCHAR(255),
    RESPONSE_JSON      BYTEA,
    STATUS             VARCHAR(20),
    ERROR_CODE         VARCHAR(100),
    ERROR_MESSAGE      VARCHAR(255),
    REQUEST_TIMESTAMP  TIMESTAMP(3),
    RESPONSE_TIMESTAMP TIMESTAMP(3),
    BT_ID              VARCHAR(10),
    PRIMARY KEY (ID)
);
