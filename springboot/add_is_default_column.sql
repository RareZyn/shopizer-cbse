-- Add is_default column to addresses table
-- This column is required by the Address entity
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT false;

-- Optional: Create an index to improve query performance when finding default addresses
CREATE INDEX IF NOT EXISTS idx_addresses_customer_default ON addresses(customer_id, is_default);
