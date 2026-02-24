CREATE TABLE IF NOT EXISTS events (
    global_id SERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS houses_view (
    house_id UUID PRIMARY KEY,
    address VARCHAR(255),
    price DECIMAL,
    status VARCHAR(50),
    owner_id UUID
);
