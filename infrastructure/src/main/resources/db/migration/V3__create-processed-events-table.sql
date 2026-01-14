CREATE TABLE processed_events (
    event_id VARCHAR(26) NOT NULL PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);