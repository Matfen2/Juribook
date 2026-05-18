CREATE TABLE outbox_events (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(50)  NOT NULL,
    aggregate_id    UUID         NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         BYTEA        NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at    TIMESTAMP WITH TIME ZONE,
    retry_count     INT          NOT NULL DEFAULT 0,
    last_error      TEXT
);

CREATE INDEX idx_outbox_unpublished
    ON outbox_events (created_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_retry_count
    ON outbox_events (retry_count)
    WHERE published_at IS NULL AND retry_count > 0;