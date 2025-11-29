-- Add is_client column to contacts table if it doesn't exist, or update existing nulls
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'contacts' AND column_name = 'is_client') THEN
        ALTER TABLE contacts ADD COLUMN is_client BOOLEAN NOT NULL DEFAULT false;
    ELSE
        UPDATE contacts SET is_client = false WHERE is_client IS NULL;
        ALTER TABLE contacts ALTER COLUMN is_client SET NOT NULL;
        ALTER TABLE contacts ALTER COLUMN is_client SET DEFAULT false;
    END IF;
END $$;

-- Add is_client column to companies table if it doesn't exist, or update existing nulls
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'companies' AND column_name = 'is_client') THEN
        ALTER TABLE companies ADD COLUMN is_client BOOLEAN NOT NULL DEFAULT false;
    ELSE
        UPDATE companies SET is_client = false WHERE is_client IS NULL;
        ALTER TABLE companies ALTER COLUMN is_client SET NOT NULL;
        ALTER TABLE companies ALTER COLUMN is_client SET DEFAULT false;
    END IF;
END $$;
