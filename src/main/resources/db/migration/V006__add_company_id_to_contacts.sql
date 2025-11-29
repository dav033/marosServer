-- Add company_id column to contacts table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'contacts' AND column_name = 'company_id') THEN
        ALTER TABLE contacts ADD COLUMN company_id BIGINT;
        ALTER TABLE contacts ADD CONSTRAINT fk_contacts_company 
            FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL;
        CREATE INDEX idx_contacts_company_id ON contacts(company_id);
    END IF;
END $$;

