-- Policies table
CREATE TABLE revet_policies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(50) NOT NULL,
    statements JSONB NOT NULL,
    tenant_id VARCHAR(255),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_on TIMESTAMPTZ NOT NULL,
    updated_on TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_revet_policies_name_tenant UNIQUE (name, tenant_id)
);

CREATE INDEX idx_revet_policies_name ON revet_policies(name);
CREATE INDEX idx_revet_policies_tenant_id ON revet_policies(tenant_id);

-- Policy attachments table
CREATE TABLE revet_policy_attachments (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL,
    principal_urn VARCHAR(1024) NOT NULL,
    attached_on TIMESTAMPTZ NOT NULL,
    attached_by VARCHAR(1024),
    CONSTRAINT fk_policy_attachments_policy FOREIGN KEY (policy_id) REFERENCES revet_policies(id) ON DELETE CASCADE,
    CONSTRAINT uq_policy_attachments_policy_principal UNIQUE (policy_id, principal_urn)
);

CREATE INDEX idx_revet_policy_attachments_policy_id ON revet_policy_attachments(policy_id);
CREATE INDEX idx_revet_policy_attachments_principal_urn ON revet_policy_attachments(principal_urn);
