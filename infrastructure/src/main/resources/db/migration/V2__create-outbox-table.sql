CREATE TABLE outbox (
    id VARCHAR(26) NOT NULL PRIMARY KEY,
    version BIGINT NOT NULL,
    aggregate_id VARCHAR(26) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,
    event_class VARCHAR(255) NOT NULL
);

CREATE INDEX outbox_status ON outbox (status);
CREATE INDEX outbox_occurred_on ON outbox(occurred_on);