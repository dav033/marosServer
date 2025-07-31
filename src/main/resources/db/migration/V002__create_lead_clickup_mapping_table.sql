-- Migration script for lead_clickup_mapping table
-- This table stores the mapping between leads and ClickUp tasks

CREATE TABLE IF NOT EXISTS lead_clickup_mapping (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL UNIQUE,
    lead_number VARCHAR(255) NOT NULL,
    clickup_task_id VARCHAR(255) NOT NULL,
    clickup_task_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_lead_clickup_mapping_lead_id ON lead_clickup_mapping(lead_id);
CREATE INDEX IF NOT EXISTS idx_lead_clickup_mapping_lead_number ON lead_clickup_mapping(lead_number);
CREATE INDEX IF NOT EXISTS idx_lead_clickup_mapping_clickup_task_id ON lead_clickup_mapping(clickup_task_id);

-- Add comment to table
COMMENT ON TABLE lead_clickup_mapping IS 'Stores mapping between Supabase leads and ClickUp tasks for webhook operations';
