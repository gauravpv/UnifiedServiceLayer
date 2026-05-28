-- =============================================================================
-- Indexes for fast cache lookup by REQUEST_HASH.
-- The repository uses `findTopByRequestHashOrderByResponseTimestampDesc` so we
-- benefit from a secondary index on RESPONSE_TIMESTAMP for the ORDER BY ... DESC
-- part of the query.
-- =============================================================================

CREATE INDEX idx_bureau_bre_request_hash
    ON bureau_bre_details (REQUEST_HASH);

CREATE INDEX idx_bureau_bre_response_ts
    ON bureau_bre_details (RESPONSE_TIMESTAMP);

CREATE INDEX idx_dedupe_bre_request_hash
    ON dedupe_bre_details (REQUEST_HASH);

CREATE INDEX idx_dedupe_bre_response_ts
    ON dedupe_bre_details (RESPONSE_TIMESTAMP);
