CREATE TABLE access_logs (
    id           UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    short_url_id UUID      NOT NULL REFERENCES short_urls (id) ON DELETE CASCADE,
    accessed_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address   VARCHAR(45),
    user_agent   TEXT
);

CREATE INDEX idx_access_logs_short_url_id ON access_logs (short_url_id);
CREATE INDEX idx_access_logs_accessed_at  ON access_logs (accessed_at);
