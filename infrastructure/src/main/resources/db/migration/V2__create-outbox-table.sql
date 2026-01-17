CREATE TABLE outbox (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    version BIGINT NOT NULL,
    aggregate_id VARCHAR(26) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payload BYTEA NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,
    payload_type VARCHAR(20) NOT NULL
);

CREATE INDEX outbox_status ON outbox (status);
CREATE INDEX outbox_occurred_on ON outbox(occurred_on);