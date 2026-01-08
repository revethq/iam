-- Users table
CREATE TABLE revet_users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_on TIMESTAMPTZ NOT NULL,
    updated_on TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_revet_users_username ON revet_users(username);
CREATE INDEX idx_revet_users_email ON revet_users(email);

-- Identity providers table
CREATE TABLE revet_identity_providers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_on TIMESTAMPTZ NOT NULL,
    updated_on TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_revet_identity_providers_name ON revet_identity_providers(name);

-- Identity provider links table
CREATE TABLE revet_identity_provider_links (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    identity_provider_id UUID NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_on TIMESTAMPTZ NOT NULL,
    updated_on TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_identity_provider_links_user FOREIGN KEY (user_id) REFERENCES revet_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_identity_provider_links_provider FOREIGN KEY (identity_provider_id) REFERENCES revet_identity_providers(id) ON DELETE CASCADE,
    CONSTRAINT uq_identity_provider_links_user_provider UNIQUE (user_id, identity_provider_id),
    CONSTRAINT uq_identity_provider_links_external_provider UNIQUE (external_id, identity_provider_id)
);

CREATE INDEX idx_revet_identity_provider_links_user_id ON revet_identity_provider_links(user_id);
CREATE INDEX idx_revet_identity_provider_links_identity_provider_id ON revet_identity_provider_links(identity_provider_id);
CREATE INDEX idx_revet_identity_provider_links_external_id ON revet_identity_provider_links(external_id);

-- Profiles table
CREATE TABLE revet_profiles (
    id UUID PRIMARY KEY,
    resource UUID,
    profile_type VARCHAR(50) NOT NULL,
    profile JSONB,
    created_on TIMESTAMPTZ NOT NULL,
    updated_on TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_revet_profiles_resource ON revet_profiles(resource);
CREATE INDEX idx_revet_profiles_profile_type ON revet_profiles(profile_type);
