-- Ensure uniqueness for contacts: email, phone, and name
-- Using unique indexes with normalization for case-insensitive fields

-- Email: unique case-insensitive, ignoring null/empty
CREATE UNIQUE INDEX IF NOT EXISTS ux_contacts_email_ci
ON contacts (lower(email))
WHERE email IS NOT NULL AND email <> '';

-- Phone: unique, ignoring null/empty
CREATE UNIQUE INDEX IF NOT EXISTS ux_contacts_phone
ON contacts (phone)
WHERE phone IS NOT NULL AND phone <> '';

-- Name: unique case-insensitive (column is NOT NULL already)
CREATE UNIQUE INDEX IF NOT EXISTS ux_contacts_name_ci
ON contacts (lower(name));
