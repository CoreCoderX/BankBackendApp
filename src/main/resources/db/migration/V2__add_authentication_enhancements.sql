-- V2__add_authentication_enhancements.sql

-- Pre-authentication sessions table
CREATE TABLE IF NOT EXISTS pre_auth_sessions (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 user_id BIGINT NOT NULL REFERENCES users(id),
    pre_auth_token VARCHAR(500) NOT NULL UNIQUE,
    authentication_state VARCHAR(50) NOT NULL,
    device_id VARCHAR(255),
    ip_address VARCHAR(100),
    user_agent VARCHAR(500),
    device_verified BOOLEAN DEFAULT FALSE,
    totp_verified BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_pre_auth_token ON pre_auth_sessions(pre_auth_token);
CREATE INDEX IF NOT EXISTS idx_pre_user_active ON pre_auth_sessions(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_pre_expires_at ON pre_auth_sessions(expires_at);

-- Token blacklist table
CREATE TABLE IF NOT EXISTS token_blacklist (
                                               id BIGSERIAL PRIMARY KEY,
                                               token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    reason VARCHAR(100),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_token_blacklist ON token_blacklist(token);
CREATE INDEX IF NOT EXISTS idx_token_bl_expires ON token_blacklist(expires_at);

-- Add trusted column to devices if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'devices' AND column_name = 'is_trusted'
    ) THEN
ALTER TABLE devices ADD COLUMN is_trusted BOOLEAN DEFAULT FALSE;
END IF;
END $$;