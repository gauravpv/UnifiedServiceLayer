-- Add HASH_JSON to bureau_bre_details (trimmed payload used for cache hash)

ALTER TABLE bureau_bre_details
    ADD COLUMN HASH_JSON MEDIUMBLOB NULL AFTER REQUEST_JSON;
