-- ==============================================================================
-- MIGRATION V2: FUND TRANSACTION MANAGEMENT & LOAN MANAGEMENT
-- ==============================================================================
-- Author: Banking System
-- Date: 2024
-- Description: Adds transaction and loan management tables to existing schema
-- ==============================================================================

-- ==============================================================================
-- PART 1: ALTER EXISTING TABLES
-- ==============================================================================

-- Add missing columns to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(150);

COMMENT ON COLUMN users.full_name IS 'Full name of the user';

-- Add missing columns to customers table
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS kyc_verified BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(150);

COMMENT ON COLUMN customers.kyc_verified IS 'Whether customer KYC is verified';
COMMENT ON COLUMN customers.full_name IS 'Computed full name from first, middle, last';

-- Update existing customer records with full names
UPDATE customers
SET full_name = CONCAT(first_name, ' ', COALESCE(middle_name || ' ', ''), last_name)
WHERE full_name IS NULL;

-- Link kyc_verified with kyc_records
UPDATE customers c
SET kyc_verified = TRUE
    FROM kyc_records kr
WHERE c.id = kr.customer_id
  AND kr.status = 'APPROVED';

-- Add missing columns to accounts table
ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS hold_balance NUMERIC(15,2) DEFAULT 0;

COMMENT ON COLUMN accounts.hold_balance IS 'Amount on hold (not available for withdrawal/transfer)';

-- ==============================================================================
-- PART 2: FUND TRANSACTION MANAGEMENT TABLES
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- TRANSACTIONS TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE transactions (
    -- Primary Key
                              id BIGSERIAL PRIMARY KEY,

    -- Transaction Identification
                              transaction_id VARCHAR(50) UNIQUE NOT NULL,
                              reference_number VARCHAR(50),

    -- Amount Details
                              amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),

    -- Transaction Metadata
                              remarks VARCHAR(500),
                              transaction_type VARCHAR(30) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              channel VARCHAR(20),

    -- Security & Audit
                              ip_address VARCHAR(100),
                              device_info VARCHAR(200),
                              is_flagged BOOLEAN DEFAULT FALSE,
                              flag_reason VARCHAR(500),

    -- Relationships
                              sender_account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
                              receiver_account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Timestamps
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,
                              created_by VARCHAR(100),
                              updated_by VARCHAR(100),

    -- Constraints
                              CONSTRAINT chk_transaction_type CHECK (
                                  transaction_type IN (
                                                       'SELF_TRANSFER',
                                                       'ACCOUNT_TRANSFER',
                                                       'BENEFICIARY_TRANSFER',
                                                       'LOAN_DISBURSEMENT',
                                                       'LOAN_REPAYMENT',
                                                       'SCHEDULED_PAYMENT',
                                                       'REVERSAL'
                                      )
                                  ),
                              CONSTRAINT chk_transaction_status CHECK (
                                  status IN (
                                             'INITIATED',
                                             'VALIDATED',
                                             'AUTHORIZED',
                                             'PENDING',
                                             'SUCCESS',
                                             'FAILED',
                                             'REVERSED',
                                             'FLAGGED'
                                      )
                                  ),
                              CONSTRAINT chk_transaction_channel CHECK (
                                  channel IN ('WEB', 'MOBILE_APP', 'API', 'ADMIN')
                                  ),
                              CONSTRAINT chk_transaction_accounts CHECK (
                                  sender_account_id IS NOT NULL OR receiver_account_id IS NOT NULL
                                  )
);

-- Indexes for transactions
CREATE INDEX idx_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_transaction_sender_account ON transactions(sender_account_id);
CREATE INDEX idx_transaction_receiver_account ON transactions(receiver_account_id);
CREATE INDEX idx_transaction_user ON transactions(user_id);
CREATE INDEX idx_transaction_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transaction_user_date ON transactions(user_id, created_at DESC);
CREATE INDEX idx_transaction_flagged ON transactions(is_flagged) WHERE is_flagged = TRUE;

COMMENT ON TABLE transactions IS 'Stores all fund transfer transactions including transfers, loan disbursements, and repayments';
COMMENT ON COLUMN transactions.transaction_id IS 'Unique transaction identifier (TXN + timestamp)';
COMMENT ON COLUMN transactions.reference_number IS 'Bank reference number for the transaction';
COMMENT ON COLUMN transactions.is_flagged IS 'Indicates if transaction is flagged for fraud detection';
COMMENT ON COLUMN transactions.channel IS 'Channel through which transaction was initiated';
COMMENT ON COLUMN accounts.hold_balance IS 'Amount temporarily held during transaction processing';

-- ------------------------------------------------------------------------------
-- USER BENEFICIARIES TABLE (Separate from existing account beneficiaries)
-- ------------------------------------------------------------------------------
-- Rename existing beneficiaries table to avoid conflict
ALTER TABLE beneficiaries RENAME TO account_beneficiaries;

-- Create new user-level beneficiaries table for fund transfers
CREATE TABLE beneficiaries (
    -- Primary Key
                               id BIGSERIAL PRIMARY KEY,

    -- Beneficiary Details
                               nickname VARCHAR(100) NOT NULL,
                               account_number VARCHAR(20) NOT NULL,
                               ifsc_code VARCHAR(11) NOT NULL,
                               bank_name VARCHAR(200) NOT NULL,
                               branch_name VARCHAR(200),

    -- Status
                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               is_verified BOOLEAN NOT NULL DEFAULT FALSE,

    -- Relationships
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Timestamps
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP,

    -- Constraints
                               CONSTRAINT unique_user_account UNIQUE(user_id, account_number),
                               CONSTRAINT chk_account_number_format CHECK (account_number ~ '^\d{10,18}$'),
    CONSTRAINT chk_ifsc_format CHECK (ifsc_code ~ '^[A-Z]{4}0[A-Z0-9]{6}$')
);

-- Indexes for beneficiaries
CREATE INDEX idx_beneficiary_user ON beneficiaries(user_id);
CREATE INDEX idx_beneficiary_account ON beneficiaries(account_number);
CREATE INDEX idx_beneficiary_active ON beneficiaries(user_id, is_active) WHERE is_active = TRUE;

COMMENT ON TABLE beneficiaries IS 'User-level saved beneficiaries for quick fund transfers';
COMMENT ON COLUMN beneficiaries.nickname IS 'User-defined friendly name for the accountBeneficiary';
COMMENT ON COLUMN beneficiaries.is_verified IS 'Whether accountBeneficiary has been verified by bank';
COMMENT ON COLUMN beneficiaries.ifsc_code IS 'Indian Financial System Code';

-- ------------------------------------------------------------------------------
-- TRANSACTION LIMITS TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE transaction_limits (
    -- Primary Key
                                    id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Limit Configuration
                                    per_transaction_limit NUMERIC(15,2) NOT NULL DEFAULT 50000.00,
                                    daily_limit NUMERIC(15,2) NOT NULL DEFAULT 200000.00,
                                    monthly_limit NUMERIC(15,2) NOT NULL DEFAULT 1000000.00,

    -- Usage Tracking
                                    daily_used NUMERIC(15,2) NOT NULL DEFAULT 0.00,
                                    monthly_used NUMERIC(15,2) NOT NULL DEFAULT 0.00,

    -- Time Tracking
                                    limit_date DATE NOT NULL,
                                    current_month INTEGER NOT NULL CHECK (current_month BETWEEN 1 AND 12),
                                    current_year INTEGER NOT NULL CHECK (current_year >= 2020),

    -- Timestamp
                                    updated_at TIMESTAMP,

    -- Constraints
                                    CONSTRAINT chk_limits_positive CHECK (
                                        per_transaction_limit > 0 AND
                                        daily_limit > 0 AND
                                        monthly_limit > 0 AND
                                        daily_used >= 0 AND
                                        monthly_used >= 0
                                        ),
                                    CONSTRAINT chk_limit_hierarchy CHECK (
                                        per_transaction_limit <= daily_limit AND
                                        daily_limit <= monthly_limit
                                        ),
                                    CONSTRAINT unique_user_date UNIQUE(user_id, limit_date)
);

-- Indexes for transaction_limits
CREATE INDEX idx_limit_user ON transaction_limits(user_id);
CREATE INDEX idx_limit_date ON transaction_limits(limit_date);
CREATE INDEX idx_limit_user_month ON transaction_limits(user_id, current_month, current_year);

COMMENT ON TABLE transaction_limits IS 'Tracks daily and monthly transaction limits per user';
COMMENT ON COLUMN transaction_limits.per_transaction_limit IS 'Maximum amount allowed per single transaction';
COMMENT ON COLUMN transaction_limits.daily_limit IS 'Maximum total amount allowed per day';
COMMENT ON COLUMN transaction_limits.monthly_limit IS 'Maximum total amount allowed per month';
COMMENT ON COLUMN transaction_limits.daily_used IS 'Total amount used today';
COMMENT ON COLUMN transaction_limits.monthly_used IS 'Total amount used this month';

-- ------------------------------------------------------------------------------
-- SCHEDULED PAYMENTS TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE scheduled_payments (
    -- Primary Key
                                    id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    beneficiary_id BIGINT NOT NULL REFERENCES beneficiaries(id) ON DELETE CASCADE,

    -- Payment Details
                                    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
                                    frequency VARCHAR(20) NOT NULL,
                                    next_execution_date DATE NOT NULL,

    -- Status
                                    status VARCHAR(20) NOT NULL,

    -- Metadata
                                    remarks VARCHAR(500),

    -- Timestamps
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP,

    -- Constraints
                                    CONSTRAINT chk_frequency CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY')),
                                    CONSTRAINT chk_scheduled_payment_status CHECK (
                                        status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')
                                        ),
                                    CONSTRAINT chk_next_execution_future CHECK (next_execution_date >= CURRENT_DATE)
);

-- Indexes for scheduled_payments
CREATE INDEX idx_scheduled_user ON scheduled_payments(user_id);
CREATE INDEX idx_scheduled_next_exec ON scheduled_payments(next_execution_date);
CREATE INDEX idx_scheduled_status ON scheduled_payments(status);
CREATE INDEX idx_scheduled_pending ON scheduled_payments(next_execution_date, status)
    WHERE status = 'PENDING';

COMMENT ON TABLE scheduled_payments IS 'Stores recurring/scheduled payment configurations';
COMMENT ON COLUMN scheduled_payments.frequency IS 'Payment frequency: DAILY, WEEKLY, MONTHLY';
COMMENT ON COLUMN scheduled_payments.next_execution_date IS 'Date when next payment should be executed';

-- ------------------------------------------------------------------------------
-- IDEMPOTENCY KEYS TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE idempotency_keys (
    -- Primary Key
                                  id BIGSERIAL PRIMARY KEY,

    -- Idempotency Key
                                  idempotency_key VARCHAR(100) UNIQUE NOT NULL,

    -- Status
                                  status VARCHAR(20) NOT NULL,

    -- Response Data
                                  response TEXT,
                                  transaction_id VARCHAR(50),

    -- Timestamp
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                  CONSTRAINT chk_idempotency_status CHECK (
                                      status IN ('SUCCESS', 'FAILED', 'PENDING')
                                      )
);

-- Indexes for idempotency_keys
CREATE UNIQUE INDEX idx_idempotency_key ON idempotency_keys(idempotency_key);
CREATE INDEX idx_idempotency_created ON idempotency_keys(created_at);
CREATE INDEX idx_idempotency_transaction ON idempotency_keys(transaction_id);

COMMENT ON TABLE idempotency_keys IS 'Prevents duplicate transaction processing from retries/double-clicks';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS 'Unique key sent by client to ensure transaction is processed only once';
COMMENT ON COLUMN idempotency_keys.response IS 'Stored response for duplicate requests';

-- ==============================================================================
-- PART 3: LOAN MANAGEMENT TABLES
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- LOANS TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE loans (
    -- Primary Key
                       id BIGSERIAL PRIMARY KEY,

    -- Loan Identification
                       loan_number VARCHAR(50) UNIQUE NOT NULL,

    -- Loan Details
                       loan_type VARCHAR(30) NOT NULL,
                       principal_amount NUMERIC(15,2) NOT NULL CHECK (principal_amount > 0),
                       interest_rate NUMERIC(5,2) NOT NULL CHECK (interest_rate > 0),
                       tenure_months INTEGER NOT NULL CHECK (tenure_months > 0),

    -- EMI Details
                       emi_amount NUMERIC(15,2) NOT NULL CHECK (emi_amount > 0),
                       remaining_principal NUMERIC(15,2) NOT NULL CHECK (remaining_principal >= 0),
                       total_interest NUMERIC(15,2),
                       total_payable NUMERIC(15,2),
                       amount_paid NUMERIC(15,2) DEFAULT 0,

    -- Status
                       status VARCHAR(20) NOT NULL,

    -- Important Dates
                       applied_date DATE NOT NULL,
                       approved_date DATE,
                       disbursed_date DATE,
                       closed_date DATE,
                       first_emi_date DATE,

    -- Additional Information
                       purpose VARCHAR(500),
                       remarks VARCHAR(500),
                       rejection_reason VARCHAR(500),
                       approved_by VARCHAR(100),

    -- Relationships
                       user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,

    -- Timestamps
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       created_by VARCHAR(100),
                       updated_by VARCHAR(100),

    -- Constraints
                       CONSTRAINT chk_loan_type CHECK (
                           loan_type IN ('PERSONAL', 'HOME', 'VEHICLE', 'EDUCATION', 'BUSINESS')
                           ),
                       CONSTRAINT chk_loan_status CHECK (
                           status IN (
                                      'PENDING',
                                      'UNDER_REVIEW',
                                      'APPROVED',
                                      'REJECTED',
                                      'DISBURSED',
                                      'ACTIVE',
                                      'OVERDUE',
                                      'CLOSED',
                                      'NPA'
                               )
                           ),
                       CONSTRAINT chk_loan_dates CHECK (
                           (approved_date IS NULL OR approved_date >= applied_date) AND
                           (disbursed_date IS NULL OR disbursed_date >= applied_date) AND
                           (closed_date IS NULL OR closed_date >= applied_date)
                           ),
                       CONSTRAINT chk_amount_paid CHECK (amount_paid >= 0),
                       CONSTRAINT chk_emi_vs_principal CHECK (emi_amount <= principal_amount),
                       CONSTRAINT chk_tenure_range CHECK (tenure_months BETWEEN 1 AND 360)
);

-- Indexes for loans
CREATE INDEX idx_loan_number ON loans(loan_number);
CREATE INDEX idx_loan_user ON loans(user_id);
CREATE INDEX idx_loan_account ON loans(account_id);
CREATE INDEX idx_loan_status ON loans(status);
CREATE INDEX idx_loan_applied_date ON loans(applied_date DESC);
CREATE INDEX idx_loan_type ON loans(loan_type);
CREATE INDEX idx_loan_user_status ON loans(user_id, status);
CREATE INDEX idx_loan_active ON loans(user_id, status)
    WHERE status IN ('ACTIVE', 'DISBURSED', 'OVERDUE');

COMMENT ON TABLE loans IS 'Stores loan applications and loan account details';
COMMENT ON COLUMN loans.loan_number IS 'Unique loan identifier (LN + timestamp)';
COMMENT ON COLUMN loans.remaining_principal IS 'Outstanding principal amount yet to be paid';
COMMENT ON COLUMN loans.status IS 'Current loan application/account status';
COMMENT ON COLUMN loans.first_emi_date IS 'Date of first EMI payment (usually 1 month after disbursement)';
COMMENT ON COLUMN loans.emi_amount IS 'Fixed monthly installment amount (EMI)';

-- ------------------------------------------------------------------------------
-- LOAN REPAYMENTS TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE loan_repayments (
    -- Primary Key
                                 id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                 loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,

    -- Payment Details
                                 payment_amount NUMERIC(15,2) NOT NULL CHECK (payment_amount > 0),
                                 principal_paid NUMERIC(15,2) NOT NULL CHECK (principal_paid >= 0),
                                 interest_paid NUMERIC(15,2) NOT NULL CHECK (interest_paid >= 0),
                                 penalty_paid NUMERIC(15,2) DEFAULT 0 CHECK (penalty_paid >= 0),
                                 remaining_balance NUMERIC(15,2) NOT NULL CHECK (remaining_balance >= 0),

    -- Payment Metadata
                                 payment_date DATE NOT NULL,
                                 status VARCHAR(20) NOT NULL,
                                 transaction_id VARCHAR(50),
                                 remarks VARCHAR(500),

    -- Timestamp
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                 CONSTRAINT chk_repayment_status CHECK (
                                     status IN ('PENDING', 'PAID', 'OVERDUE', 'PARTIAL')
                                     ),
                                 CONSTRAINT chk_payment_allocation CHECK (
                                     payment_amount = principal_paid + interest_paid + penalty_paid
                                     )
);

-- Indexes for loan_repayments
CREATE INDEX idx_repayment_loan ON loan_repayments(loan_id);
CREATE INDEX idx_repayment_date ON loan_repayments(payment_date DESC);
CREATE INDEX idx_repayment_status ON loan_repayments(status);
CREATE INDEX idx_repayment_transaction ON loan_repayments(transaction_id);
CREATE INDEX idx_repayment_loan_date ON loan_repayments(loan_id, payment_date DESC);

COMMENT ON TABLE loan_repayments IS 'Stores all loan repayment/EMI payment transactions';
COMMENT ON COLUMN loan_repayments.payment_amount IS 'Total amount paid in this EMI payment';
COMMENT ON COLUMN loan_repayments.principal_paid IS 'Principal portion of the payment';
COMMENT ON COLUMN loan_repayments.interest_paid IS 'Interest portion of the payment';
COMMENT ON COLUMN loan_repayments.penalty_paid IS 'Late payment penalty portion (if any)';
COMMENT ON COLUMN loan_repayments.remaining_balance IS 'Principal balance remaining after this payment';

-- ------------------------------------------------------------------------------
-- LOAN SCHEDULES TABLE (Amortization Schedule)
-- ------------------------------------------------------------------------------
CREATE TABLE loan_schedules (
    -- Primary Key
                                id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,

    -- EMI Details
                                emi_number INTEGER NOT NULL CHECK (emi_number > 0),
                                due_date DATE NOT NULL,
                                emi_amount NUMERIC(15,2) NOT NULL CHECK (emi_amount > 0),

    -- EMI Breakdown
                                principal_component NUMERIC(15,2) NOT NULL CHECK (principal_component >= 0),
                                interest_component NUMERIC(15,2) NOT NULL CHECK (interest_component >= 0),
                                outstanding_principal NUMERIC(15,2) NOT NULL CHECK (outstanding_principal >= 0),

    -- Status
                                status VARCHAR(20) NOT NULL,
                                paid_date DATE,

    -- Timestamps
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP,

    -- Constraints
                                CONSTRAINT unique_loan_emi UNIQUE(loan_id, emi_number),
                                CONSTRAINT chk_schedule_status CHECK (
                                    status IN ('PENDING', 'PAID', 'OVERDUE', 'PARTIAL')
                                    ),
                                CONSTRAINT chk_emi_components CHECK (
                                    emi_amount = principal_component + interest_component
                                    ),
                                CONSTRAINT chk_paid_date CHECK (
                                    (status = 'PAID' AND paid_date IS NOT NULL) OR
                                    (status != 'PAID' AND paid_date IS NULL)
                                    )
);

-- Indexes for loan_schedules
CREATE INDEX idx_schedule_loan ON loan_schedules(loan_id);
CREATE INDEX idx_schedule_due_date ON loan_schedules(due_date);
CREATE INDEX idx_schedule_status ON loan_schedules(status);
CREATE INDEX idx_schedule_loan_emi ON loan_schedules(loan_id, emi_number);
CREATE INDEX idx_schedule_pending ON loan_schedules(loan_id, status)
    WHERE status = 'PENDING';
CREATE INDEX idx_schedule_overdue ON loan_schedules(due_date, status)
    WHERE status = 'PENDING' AND due_date < CURRENT_DATE;

COMMENT ON TABLE loan_schedules IS 'Stores EMI amortization schedule for each loan';
COMMENT ON COLUMN loan_schedules.emi_number IS 'EMI installment number (1, 2, 3, ...)';
COMMENT ON COLUMN loan_schedules.due_date IS 'Date when this EMI is due for payment';
COMMENT ON COLUMN loan_schedules.principal_component IS 'Principal portion of this EMI';
COMMENT ON COLUMN loan_schedules.interest_component IS 'Interest portion of this EMI';
COMMENT ON COLUMN loan_schedules.outstanding_principal IS 'Principal balance remaining after this EMI is paid';

-- ------------------------------------------------------------------------------
-- LOAN PENALTIES TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE loan_penalties (
    -- Primary Key
                                id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,

    -- Penalty Details
                                amount NUMERIC(10,2) NOT NULL CHECK (amount > 0),
                                reason VARCHAR(200) NOT NULL,

    -- Status
                                is_paid BOOLEAN NOT NULL DEFAULT FALSE,

    -- Timestamp
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for loan_penalties
CREATE INDEX idx_penalty_loan ON loan_penalties(loan_id);
CREATE INDEX idx_penalty_created ON loan_penalties(created_at DESC);
CREATE INDEX idx_penalty_unpaid ON loan_penalties(loan_id, is_paid)
    WHERE is_paid = FALSE;

COMMENT ON TABLE loan_penalties IS 'Stores late payment penalties and charges for loans';
COMMENT ON COLUMN loan_penalties.amount IS 'Penalty/late fee amount charged';
COMMENT ON COLUMN loan_penalties.reason IS 'Reason for penalty (e.g., EMI #X overdue by Y days)';
COMMENT ON COLUMN loan_penalties.is_paid IS 'Whether penalty has been paid';

-- ==============================================================================
-- PART 4: SEQUENCES
-- ==============================================================================

-- Transaction Management Sequences
CREATE SEQUENCE IF NOT EXISTS transaction_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS beneficiary_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS limit_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS scheduled_payment_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS idempotency_sequence START 1 INCREMENT 1;

-- Loan Management Sequences
CREATE SEQUENCE IF NOT EXISTS loan_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS repayment_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS schedule_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS penalty_sequence START 1 INCREMENT 1;

-- ==============================================================================
-- PART 5: TRIGGERS
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- Trigger: Auto-reset daily/monthly transaction limits
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION reset_transaction_limits()
RETURNS TRIGGER AS $$
BEGIN
    -- Reset daily usage if date changed
    IF NEW.current_date != OLD.current_date THEN
        NEW.daily_used := 0;
END IF;

    -- Reset monthly usage if month changed
    IF NEW.current_month != OLD.current_month OR NEW.current_year != OLD.current_year THEN
        NEW.monthly_used := 0;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_reset_transaction_limits
    BEFORE UPDATE ON transaction_limits
    FOR EACH ROW
    EXECUTE FUNCTION reset_transaction_limits();

-- ------------------------------------------------------------------------------
-- Trigger: Auto-close loan when fully paid
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION auto_close_loan()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.remaining_principal = 0 AND NEW.status != 'CLOSED' THEN
        NEW.status := 'CLOSED';
        NEW.closed_date := CURRENT_DATE;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_auto_close_loan
    BEFORE UPDATE ON loans
    FOR EACH ROW
    WHEN (NEW.remaining_principal = 0 AND OLD.status != 'CLOSED')
EXECUTE FUNCTION auto_close_loan();

-- ------------------------------------------------------------------------------
-- Trigger: Update customer full_name automatically
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_customer_full_name()
RETURNS TRIGGER AS $$
BEGIN
    NEW.full_name := CONCAT(
        NEW.first_name,
        ' ',
        COALESCE(NEW.middle_name || ' ', ''),
        NEW.last_name
    );
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_customer_full_name
    BEFORE INSERT OR UPDATE ON customers
                         FOR EACH ROW
                         EXECUTE FUNCTION update_customer_full_name();

-- ------------------------------------------------------------------------------
-- Trigger: Transaction audit log
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transaction_audit_log (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     transaction_id BIGINT,
                                                     action VARCHAR(10),
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE OR REPLACE FUNCTION log_transaction_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.status != NEW.status THEN
        INSERT INTO transaction_audit_log (
            transaction_id, action, old_status, new_status, changed_by
        ) VALUES (
            NEW.id, 'UPDATE', OLD.status, NEW.status, NEW.updated_by
        );
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_log_transaction_changes
    AFTER UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION log_transaction_changes();

-- ==============================================================================
-- PART 6: VIEWS (FOR REPORTING & ANALYTICS)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- View: Active Loans Summary
-- ------------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_active_loans AS
SELECT
    l.id,
    l.loan_number,
    l.loan_type,
    l.principal_amount,
    l.remaining_principal,
    l.emi_amount,
    l.status,
    u.email AS user_email,
    u.full_name AS user_name,
    a.account_number,
    (SELECT COUNT(*) FROM loan_schedules WHERE loan_id = l.id AND status = 'PAID') AS emis_paid,
    (SELECT COUNT(*) FROM loan_schedules WHERE loan_id = l.id AND status = 'PENDING') AS emis_pending,
    (SELECT SUM(amount) FROM loan_penalties WHERE loan_id = l.id AND is_paid = FALSE) AS penalty_due
FROM loans l
         JOIN users u ON l.user_id = u.id
         JOIN accounts a ON l.account_id = a.id
WHERE l.status IN ('ACTIVE', 'DISBURSED', 'OVERDUE');

COMMENT ON VIEW v_active_loans IS 'Summary of all active loans with EMI and penalty details';

-- ------------------------------------------------------------------------------
-- View: Daily Transaction Summary
-- ------------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_daily_transaction_summary AS
SELECT
    DATE(created_at) AS transaction_date,
    transaction_type,
    status,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS average_amount,
    MIN(amount) AS min_amount,
    MAX(amount) AS max_amount
FROM transactions
GROUP BY DATE(created_at), transaction_type, status
ORDER BY transaction_date DESC, transaction_type;

COMMENT ON VIEW v_daily_transaction_summary IS 'Daily aggregated transaction statistics';

-- ------------------------------------------------------------------------------
-- View: User Loan Summary
-- ------------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_user_loan_summary AS
SELECT
    u.id AS user_id,
    u.email,
    u.full_name,
    COUNT(l.id) AS total_loans,
    COUNT(CASE WHEN l.status IN ('ACTIVE', 'DISBURSED', 'OVERDUE') THEN 1 END) AS active_loans,
    COUNT(CASE WHEN l.status = 'CLOSED' THEN 1 END) AS closed_loans,
    SUM(CASE WHEN l.status IN ('ACTIVE', 'DISBURSED', 'OVERDUE') THEN l.remaining_principal ELSE 0 END) AS total_outstanding
FROM users u
         LEFT JOIN loans l ON u.id = l.user_id
GROUP BY u.id, u.email, u.full_name;

COMMENT ON VIEW v_user_loan_summary IS 'Per-user loan account summary';

-- ==============================================================================
-- PART 7: INSERT DEFAULT DATA
-- ==============================================================================

-- Insert default transaction limits for existing users
INSERT INTO transaction_limits (user_id, limit_date, current_month, current_year)
SELECT
    id,
    CURRENT_DATE,
    EXTRACT(MONTH FROM CURRENT_DATE)::INTEGER,
    EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER
FROM users
WHERE id NOT IN (SELECT user_id FROM transaction_limits)
    ON CONFLICT (user_id, limit_date) DO NOTHING;
IF NEW.limit_date != OLD.limit_date THEN
  NEW.daily_used := 0;
END IF;


-- ==============================================================================
-- RENAME EXISTING BENEFICIARIES TABLE
-- ==============================================================================

-- Rename old beneficiaries table to avoid conflict


-- Update indexes
ALTER INDEX idx_account_id RENAME TO idx_account_beneficiary_account;
ALTER INDEX idx_beneficiary_account RENAME TO idx_account_beneficiary_account_number;

-- ==============================================================================
-- CREATE NEW USER-LEVEL BENEFICIARIES TABLE
-- ==============================================================================

CREATE TABLE beneficiaries (
                               id BIGSERIAL PRIMARY KEY,

                               nickname VARCHAR(100) NOT NULL,
                               account_number VARCHAR(20) NOT NULL,
                               ifsc_code VARCHAR(11) NOT NULL,
                               bank_name VARCHAR(200) NOT NULL,
                               branch_name VARCHAR(200),

                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               is_verified BOOLEAN NOT NULL DEFAULT FALSE,

                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP,

                               CONSTRAINT unique_user_account UNIQUE(user_id, account_number),
                               CONSTRAINT chk_account_number_format CHECK (account_number ~ '^\d{10,18}$'),
    CONSTRAINT chk_ifsc_format CHECK (ifsc_code ~ '^[A-Z]{4}0[A-Z0-9]{6}$')
);

CREATE INDEX idx_beneficiary_user ON beneficiaries(user_id);
CREATE INDEX idx_beneficiary_account ON beneficiaries(account_number);
CREATE INDEX idx_beneficiary_active ON beneficiaries(user_id, is_active) WHERE is_active = TRUE;

COMMENT ON TABLE beneficiaries IS 'User-level saved beneficiaries for fund transfers';
COMMENT ON TABLE account_beneficiaries IS 'Account-level beneficiaries (legacy/existing functionality)';
-- ==============================================================================
-- PART 8: GRANT PERMISSIONS (OPTIONAL - FOR PRODUCTION)
-- ==============================================================================

-- Grant permissions to application user (replace 'banking_app_user' with your actual DB user)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO banking_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO banking_app_user;

-- ==============================================================================
-- MIGRATION COMPLETE
-- ==============================================================================

-- Verify migration
DO $$
DECLARE
table_count INTEGER;
BEGIN
SELECT COUNT(*) INTO table_count
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
                     'loan_penalties'
    );

IF table_count = 9 THEN
        RAISE NOTICE '✓ Migration V2 completed successfully. All 9 tables created.';
ELSE
        RAISE WARNING '⚠ Migration incomplete. Expected 9 tables, found %', table_count;
END IF;
END $$;


