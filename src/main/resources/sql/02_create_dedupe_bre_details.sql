-- =============================================================================
-- Table: dedupe_bre_details
-- Purpose: Cache + audit log for downstream Dedupe (BRE) service responses.
-- Lookup key:        REQUEST_HASH (SHA-256 hex of the canonicalized `data` JSON).
-- Staleness anchor:  RESPONSE_TIMESTAMP (compared against now - 30 days).
-- Type discriminator: BT_ID = 'DEDUPE' for every row in this table.
-- =============================================================================

CREATE TABLE IF NOT EXISTS dedupe_bre_details (
    ID                 BIGINT          NOT NULL AUTO_INCREMENT,
    REQUEST_JSON       MEDIUMBLOB      NULL,
    REQUEST_HASH       VARCHAR(255)    NULL,
    RESPONSE_JSON      MEDIUMBLOB      NULL,
    STATUS             VARCHAR(20)     NULL,
    ERROR_CODE         VARCHAR(100)    NULL,
    ERROR_MESSAGE      VARCHAR(255)    NULL,
    REQUEST_TIMESTAMP  TIMESTAMP(3)    NULL,
    RESPONSE_TIMESTAMP TIMESTAMP(3)    NULL,
    BT_ID              VARCHAR(10)     NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
