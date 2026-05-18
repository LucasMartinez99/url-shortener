CREATE TABLE short_urls (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    original_url     TEXT         NOT NULL,
    short_code       VARCHAR(20)  NOT NULL UNIQUE,
    custom_alias     VARCHAR(50)  UNIQUE,
    user_id          UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at       TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_accessed_at TIMESTAMP,
    click_count      BIGINT       NOT NULL DEFAULT 0,
    active           BOOLEAN      NOT NULL DEFAULT true
);

-- Most queries hit short_code or custom_alias when redirecting
CREATE INDEX idx_short_urls_short_code   ON short_urls (short_code);
CREATE INDEX idx_short_urls_custom_alias ON short_urls (custom_alias);
CREATE INDEX idx_short_urls_user_id      ON short_urls (user_id);
