-- =============================================================================
-- PostgreSQL
-- Table: usl_bre_transactions
-- Purpose: Unified audit log for all Bureau and Dedupe requests processed by USL.
-- Stores unencrypted full request/response JSON (config + data), timing split,
-- and service discriminator (BT_ID = 'BUREAU' or 'DEDUPE').
-- Timing format: m:ss:SSS  e.g. 1:02:346
-- =============================================================================

CREATE TABLE IF NOT EXISTS usl_bre_transactions (
    ID                 BIGSERIAL,
    REFERENCE_ID       VARCHAR(100),
    SOURCE_SYSTEM      VARCHAR(50),
    REQUEST_JSON       BYTEA,
    REQUEST_HASH       VARCHAR(255),
    RESPONSE_JSON      BYTEA,
    STATUS             VARCHAR(20),
    ERROR_CODE         VARCHAR(100),
    ERROR_MESSAGE      VARCHAR(255),
    REQUEST_TIMESTAMP  TIMESTAMP(3),
    RESPONSE_TIMESTAMP TIMESTAMP(3),
    BT_ID              VARCHAR(10),
    CACHE_OUTCOME      VARCHAR(20),
    TOTAL_TIME         VARCHAR(20),
    SYSTEM_TIME        VARCHAR(20),
    SERVICE_TIME       VARCHAR(20),
    PRIMARY KEY (ID)
);

CREATE INDEX IF NOT EXISTS idx_usl_bre_bt_id
    ON usl_bre_transactions (BT_ID);

CREATE INDEX IF NOT EXISTS idx_usl_bre_request_hash
    ON usl_bre_transactions (REQUEST_HASH);

CREATE INDEX IF NOT EXISTS idx_usl_bre_reference_id
    ON usl_bre_transactions (REFERENCE_ID);

CREATE INDEX IF NOT EXISTS idx_usl_bre_request_ts
    ON usl_bre_transactions (REQUEST_TIMESTAMP);

CREATE INDEX IF NOT EXISTS idx_usl_bre_response_ts
    ON usl_bre_transactions (RESPONSE_TIMESTAMP);
