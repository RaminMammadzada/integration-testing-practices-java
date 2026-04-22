CREATE TABLE IF NOT EXISTS domain_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    producer VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payload TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS emr_documents (
    id UUID PRIMARY KEY,
    therapy_session_id UUID,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    export_status VARCHAR(50) NOT NULL,
    retry_count INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
