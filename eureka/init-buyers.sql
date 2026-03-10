CREATE TABLE IF NOT EXISTS events (
    global_id SERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS buyers_view (
    buyer_id UUID PRIMARY KEY,
    name VARCHAR(255),
    balance DECIMAL
);

CREATE TABLE IF NOT EXISTS buyer_outbox (
    id UUID PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    message_key VARCHAR(255),
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
