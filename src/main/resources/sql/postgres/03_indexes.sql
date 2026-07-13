-- =============================================================================
-- PostgreSQL
-- Indexes for fast cache lookup by REQUEST_HASH.
-- The repository uses findTopByRequestHashOrderByResponseTimestampDesc, so a
-- secondary index on RESPONSE_TIMESTAMP helps the ORDER BY ... DESC.
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_bureau_bre_request_hash
    ON bureau_bre_details (REQUEST_HASH);

CREATE INDEX IF NOT EXISTS idx_bureau_bre_response_ts
    ON bureau_bre_details (RESPONSE_TIMESTAMP);

CREATE INDEX IF NOT EXISTS idx_dedupe_bre_request_hash
    ON dedupe_bre_details (REQUEST_HASH);

CREATE INDEX IF NOT EXISTS idx_dedupe_bre_response_ts
    ON dedupe_bre_details (RESPONSE_TIMESTAMP);
