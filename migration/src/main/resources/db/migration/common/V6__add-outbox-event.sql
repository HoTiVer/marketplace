
CREATE TABLE outbox_event (
    id UUID,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP,
    CONSTRAINT pk_outbox_event PRIMARY KEY (id)
);