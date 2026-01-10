CREATE TABLE import_jobs (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    version BIGINT NOT NULL,
    file_ref VARCHAR(255) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    processed_rows BIGINT
);