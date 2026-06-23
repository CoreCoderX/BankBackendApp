--V3__fix_schema_constraints.sql

-- Fix audit_logs user_agent column size
ALTER TABLE audit_logs ALTER COLUMN user_agent TYPE VARCHAR(2000);

-- Fix credit_cards numeric precision issues
ALTER TABLE credit_cards ALTER COLUMN interest_rate TYPE NUMERIC(6,2);
ALTER TABLE credit_cards ALTER COLUMN credit_limit TYPE NUMERIC(18,2);
ALTER TABLE credit_cards ALTER COLUMN available_credit TYPE NUMERIC(18,2);
ALTER TABLE credit_cards ALTER COLUMN outstanding_balance TYPE NUMERIC(18,2);

-- Make credit card columns nullable for pending applications
ALTER TABLE credit_cards ALTER COLUMN card_number DROP NOT NULL;
ALTER TABLE credit_cards ALTER COLUMN cvv DROP NOT NULL;
ALTER TABLE credit_cards ALTER COLUMN expiry_date DROP NOT NULL;
