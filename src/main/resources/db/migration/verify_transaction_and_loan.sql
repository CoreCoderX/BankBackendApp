-- ==============================================================================
-- VERIFICATION QUERIES
-- ==============================================================================

-- 1. Check all new tables exist
SELECT table_name,
       pg_size_pretty(pg_total_relation_size(quote_ident(table_name))) AS size
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN (
    'transactions',
    'beneficiaries',
    'transaction_limits',
    'scheduled_payments',
    'idempotency_keys',
    'loans',
    'loan_repayments',
    'loan_schedules',
    'loan_penalties',
    'transaction_audit_log'
    )
ORDER BY table_name;

-- 2. Check all indexes
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN (
                    'transactions',
                    'beneficiaries',
                    'transaction_limits',
                    'loans',
                    'loan_repayments',
                    'loan_schedules'
    )
ORDER BY tablename, indexname;

-- 3. Check all sequences
SELECT sequence_name, last_value
FROM information_schema.sequences
WHERE sequence_schema = 'public'
    AND sequence_name LIKE '%transaction%'
   OR sequence_name LIKE '%loan%'
   OR sequence_name LIKE '%accountBeneficiary%'
ORDER BY sequence_name;

-- 4. Check all constraints
SELECT conname, contype, conrelid::regclass AS table_name
FROM pg_constraint
WHERE connamespace = 'public'::regnamespace
  AND conrelid::regclass::text IN (
      'transactions',
      'loans',
      'loan_repayments',
      'loan_schedules'
  )
ORDER BY table_name, conname;

-- 5. Check triggers
SELECT trigger_name, event_object_table, action_statement
FROM information_schema.triggers
WHERE trigger_schema = 'public'
ORDER BY event_object_table, trigger_name;

-- 6. Check views
SELECT table_name, view_definition
FROM information_schema.views
WHERE table_schema = 'public'
  AND table_name LIKE 'v_%'
ORDER BY table_name;

-- 7. Verify column additions to existing tables
SELECT
    column_name,
    data_type,
    column_default,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('users', 'customers', 'accounts')
  AND column_name IN ('full_name', 'kyc_verified', 'hold_balance')
ORDER BY table_name, column_name;

SELECT table_name,
       (SELECT COUNT(*) FROM information_schema.columns
        WHERE table_name = t.table_name) AS column_count
FROM information_schema.tables t
WHERE table_schema = 'public'
  AND table_name LIKE '%beneficiar%'
ORDER BY table_name;
-- Check if beneficiaries table exists
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' AND table_name = 'beneficiaries';

-- Check if it has data
SELECT COUNT(*) FROM beneficiaries;

-- Check if account_beneficiaries already exists
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' AND table_name = 'account_beneficiaries';