-- =============================================================================
-- Table: usl_bre_transactions
-- Purpose: Unified audit log for all Bureau and Dedupe requests processed by USL.
-- Stores unencrypted full request/response JSON (config + data), timing split,
-- and service discriminator (BT_ID = 'BUREAU' or 'DEDUPE').
-- =============================================================================

CREATE TABLE IF NOT EXISTS usl_bre_transactions (
    ID                      BIGINT          NOT NULL AUTO_INCREMENT,
    REFERENCE_ID            VARCHAR(100)    NULL,
    SOURCE_SYSTEM           VARCHAR(50)     NULL,
    REQUEST_JSON            MEDIUMBLOB      NULL,
    REQUEST_HASH            VARCHAR(255)    NULL,
    RESPONSE_JSON           MEDIUMBLOB      NULL,
    STATUS                  VARCHAR(20)     NULL,
    ERROR_CODE              VARCHAR(100)    NULL,
    ERROR_MESSAGE           VARCHAR(255)    NULL,
    REQUEST_TIMESTAMP       TIMESTAMP(3)    NULL,
    RESPONSE_TIMESTAMP      TIMESTAMP(3)    NULL,
    BT_ID                   VARCHAR(10)     NULL,
    CACHE_OUTCOME           VARCHAR(20)     NULL,
    TOTAL_TIME              VARCHAR(20)     NULL,
    SYSTEM_TIME             VARCHAR(20)     NULL,
    SERVICE_TIME            VARCHAR(20)     NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for reporting and lookup
CREATE INDEX idx_usl_bre_bt_id
    ON usl_bre_transactions (BT_ID);

CREATE INDEX idx_usl_bre_request_hash
    ON usl_bre_transactions (REQUEST_HASH);

CREATE INDEX idx_usl_bre_reference_id
    ON usl_bre_transactions (REFERENCE_ID);

CREATE INDEX idx_usl_bre_request_ts
    ON usl_bre_transactions (REQUEST_TIMESTAMP);

CREATE INDEX idx_usl_bre_response_ts
    ON usl_bre_transactions (RESPONSE_TIMESTAMP);
