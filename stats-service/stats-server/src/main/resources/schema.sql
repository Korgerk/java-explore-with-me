CREATE TABLE IF NOT EXISTS hits (
    id BIGSERIAL PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(2048) NOT NULL,
    ip VARCHAR(64) NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hits_uri ON hits (uri);
CREATE INDEX IF NOT EXISTS idx_hits_created ON hits (created);
CREATE INDEX IF NOT EXISTS idx_hits_uri_created ON hits (uri, created);
