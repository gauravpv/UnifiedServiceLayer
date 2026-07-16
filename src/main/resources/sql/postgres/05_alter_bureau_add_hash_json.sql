-- Add HASH_JSON to bureau_bre_details (trimmed payload used for cache hash)

ALTER TABLE bureau_bre_details
    ADD COLUMN IF NOT EXISTS HASH_JSON BYTEA;
