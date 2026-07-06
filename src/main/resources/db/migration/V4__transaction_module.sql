-- V4__transaction_module.sql

-- ================================
-- TRANSACTION MODULE SCHEMA
-- ================================

-- Transaction Categories Master
CREATE TABLE transaction_categories (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(50) NOT NULL UNIQUE,
                                        description VARCHAR(200),
                                        icon VARCHAR(100),
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Main Transactions Table
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              transaction_id VARCHAR(50) NOT NULL UNIQUE,
                              idempotency_key VARCHAR(100) UNIQUE,

    -- Accounts
                              sender_account_id BIGINT REFERENCES accounts(id),
                              receiver_account_id BIGINT REFERENCES accounts(id),

    -- External transfer details
                              receiver_account_number VARCHAR(20),
                              receiver_ifsc_code VARCHAR(20),
                              receiver_name VARCHAR(100),
                              receiver_bank_name VARCHAR(100),

    -- Transaction details
                              amount NUMERIC(18,2) NOT NULL,
                              currency VARCHAR(3) DEFAULT 'INR',
                              transaction_type VARCHAR(50) NOT NULL,
                              transaction_mode VARCHAR(50) NOT NULL,
                              payment_method VARCHAR(50) NOT NULL,

    -- Status & workflow
                              status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
                              previous_status VARCHAR(30),

    -- Category & description
                              category_id BIGINT REFERENCES transaction_categories(id),
                              description VARCHAR(500),
                              remarks VARCHAR(500),
                              reference_number VARCHAR(100),
                              utr_number VARCHAR(100),

    -- Fees & charges
                              transaction_fee NUMERIC(10,2) DEFAULT 0,
                              gst NUMERIC(10,2) DEFAULT 0,
                              total_amount NUMERIC(18,2) NOT NULL,

    -- Balance snapshots
                              sender_balance_before NUMERIC(18,2),
                              sender_balance_after NUMERIC(18,2),
                              receiver_balance_before NUMERIC(18,2),
                              receiver_balance_after NUMERIC(18,2),

    -- Timestamps
                              initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              validated_at TIMESTAMP,
                              processing_at TIMESTAMP,
                              completed_at TIMESTAMP,
                              failed_at TIMESTAMP,
                              reversed_at TIMESTAMP,

    -- Failure & reversal
                              failure_reason VARCHAR(500),
                              reversal_reason VARCHAR(500),
                              reversal_transaction_id BIGINT REFERENCES transactions(id),

    -- Security & fraud
                              ip_address VARCHAR(100),
                              device_id VARCHAR(255),
                              location VARCHAR(200),
                              fraud_score NUMERIC(5,2) DEFAULT 0,
                              is_flagged BOOLEAN DEFAULT FALSE,

    -- Metadata
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,

    -- Optimistic locking
                              version INT DEFAULT 0
);

CREATE INDEX idx_txn_sender_account ON transactions(sender_account_id);
CREATE INDEX idx_txn_receiver_account ON transactions(receiver_account_id);
CREATE INDEX idx_txn_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_txn_idempotency_key ON transactions(idempotency_key);
CREATE INDEX idx_txn_status ON transactions(status);
CREATE INDEX idx_txn_type ON transactions(transaction_type);
CREATE INDEX idx_txn_initiated_at ON transactions(initiated_at);
CREATE INDEX idx_txn_completed_at ON transactions(completed_at);

-- Transaction Metadata (additional key-value data)
CREATE TABLE transaction_metadata (
                                      id BIGSERIAL PRIMARY KEY,
                                      transaction_id BIGINT NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
                                      meta_key VARCHAR(100) NOT NULL,
                                      meta_value TEXT,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_txn_meta_txn_id ON transaction_metadata(transaction_id);

-- Transaction Limits
CREATE TABLE transaction_limits (
                                    id BIGSERIAL PRIMARY KEY,
                                    customer_id BIGINT NOT NULL REFERENCES customers(id),

    -- Per transaction limits
                                    per_transaction_limit NUMERIC(18,2) DEFAULT 50000,

    -- Daily limits
                                    daily_upi_limit NUMERIC(18,2) DEFAULT 100000,
                                    daily_imps_limit NUMERIC(18,2) DEFAULT 200000,
                                    daily_neft_limit NUMERIC(18,2) DEFAULT 1000000,
                                    daily_rtgs_limit NUMERIC(18,2) DEFAULT 5000000,
                                    daily_qr_limit NUMERIC(18,2) DEFAULT 100000,

    -- Monthly limits
                                    monthly_transfer_limit NUMERIC(18,2) DEFAULT 10000000,

    -- Tracking (resets daily)
                                    daily_upi_used NUMERIC(18,2) DEFAULT 0,
                                    daily_imps_used NUMERIC(18,2) DEFAULT 0,
                                    daily_neft_used NUMERIC(18,2) DEFAULT 0,
                                    daily_rtgs_used NUMERIC(18,2) DEFAULT 0,
                                    daily_qr_used NUMERIC(18,2) DEFAULT 0,
                                    monthly_used NUMERIC(18,2) DEFAULT 0,

                                    last_reset_date DATE DEFAULT CURRENT_DATE,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_txn_limit_customer ON transaction_limits(customer_id);

-- Banks Master (for external transfers)
CREATE TABLE banks (
                       id BIGSERIAL PRIMARY KEY,
                       bank_code VARCHAR(10) NOT NULL UNIQUE,
                       bank_name VARCHAR(100) NOT NULL,
                       ifsc_prefix VARCHAR(4) NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_banks_code ON banks(bank_code);
CREATE INDEX idx_banks_ifsc_prefix ON banks(ifsc_prefix);

-- UPI Profiles
CREATE TABLE upi_profiles (
                              id BIGSERIAL PRIMARY KEY,
                              customer_id BIGINT NOT NULL UNIQUE REFERENCES customers(id),
                              primary_upi_id_id BIGINT,
                              is_active BOOLEAN DEFAULT TRUE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP
);

-- UPI IDs (multiple per profile)
CREATE TABLE upi_ids (
                         id BIGSERIAL PRIMARY KEY,
                         upi_profile_id BIGINT NOT NULL REFERENCES upi_profiles(id) ON DELETE CASCADE,
                         upi_id VARCHAR(100) NOT NULL UNIQUE,
                         linked_account_id BIGINT REFERENCES accounts(id),
                         is_primary BOOLEAN DEFAULT FALSE,
                         is_active BOOLEAN DEFAULT TRUE,
                         verified BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP
);

CREATE INDEX idx_upi_id_profile ON upi_ids(upi_profile_id);
CREATE INDEX idx_upi_id_upi_id ON upi_ids(upi_id);

-- Add foreign key after upi_ids table is created
ALTER TABLE upi_profiles ADD CONSTRAINT fk_upi_primary_id
    FOREIGN KEY (primary_upi_id_id) REFERENCES upi_ids(id);

-- UPI PIN
CREATE TABLE upi_pins (
                          id BIGSERIAL PRIMARY KEY,
                          upi_profile_id BIGINT NOT NULL UNIQUE REFERENCES upi_profiles(id) ON DELETE CASCADE,
                          pin_hash VARCHAR(255) NOT NULL,
                          failed_attempts INT DEFAULT 0,
                          is_locked BOOLEAN DEFAULT FALSE,
                          locked_until TIMESTAMP,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          last_used_at TIMESTAMP
);

-- UPI Collect Requests (Money Requests)
CREATE TABLE upi_collect_requests (
                                      id BIGSERIAL PRIMARY KEY,
                                      request_id VARCHAR(50) NOT NULL UNIQUE,
                                      requester_upi_id VARCHAR(100) NOT NULL,
                                      payer_upi_id VARCHAR(100) NOT NULL,
                                      amount NUMERIC(18,2) NOT NULL,
                                      description VARCHAR(500),
                                      status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                      expires_at TIMESTAMP NOT NULL,
                                      responded_at TIMESTAMP,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_upi_collect_requester ON upi_collect_requests(requester_upi_id);
CREATE INDEX idx_upi_collect_payer ON upi_collect_requests(payer_upi_id);
CREATE INDEX idx_upi_collect_status ON upi_collect_requests(status);

-- UPI QR Codes
CREATE TABLE upi_qr_codes (
                              id BIGSERIAL PRIMARY KEY,
                              qr_id VARCHAR(50) NOT NULL UNIQUE,
                              upi_id VARCHAR(100) NOT NULL,
                              qr_type VARCHAR(20) NOT NULL,
                              amount NUMERIC(18,2),
                              description VARCHAR(500),
                              qr_data TEXT NOT NULL,
                              qr_image_base64 TEXT,
                              is_active BOOLEAN DEFAULT TRUE,
                              expires_at TIMESTAMP,
                              scan_count INT DEFAULT 0,
                              max_scans INT,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- UPI Transactions
CREATE TABLE upi_transactions (
                                  id BIGSERIAL PRIMARY KEY,
                                  transaction_id BIGINT NOT NULL UNIQUE REFERENCES transactions(id) ON DELETE CASCADE,
                                  sender_upi_id VARCHAR(100) NOT NULL,
                                  receiver_upi_id VARCHAR(100) NOT NULL,
                                  vpa_verified BOOLEAN DEFAULT FALSE,
                                  collect_request_id BIGINT REFERENCES upi_collect_requests(id),
                                  qr_code_id BIGINT REFERENCES upi_qr_codes(id),
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_upi_txn_sender ON upi_transactions(sender_upi_id);
CREATE INDEX idx_upi_txn_receiver ON upi_transactions(receiver_upi_id);


CREATE INDEX idx_upi_qr_upi_id ON upi_qr_codes(upi_id);
CREATE INDEX idx_upi_qr_type ON upi_qr_codes(qr_type);

-- Scheduled Payments
CREATE TABLE scheduled_payments (
                                    id BIGSERIAL PRIMARY KEY,
                                    customer_id BIGINT NOT NULL REFERENCES customers(id),
                                    sender_account_id BIGINT NOT NULL REFERENCES accounts(id),
                                    receiver_account_id BIGINT REFERENCES accounts(id),
                                    beneficiary_id BIGINT REFERENCES beneficiaries(id),

    -- External transfer
                                    receiver_account_number VARCHAR(20),
                                    receiver_ifsc_code VARCHAR(20),
                                    receiver_name VARCHAR(100),

                                    amount NUMERIC(18,2) NOT NULL,
                                    transaction_type VARCHAR(50) NOT NULL,
                                    payment_method VARCHAR(50) NOT NULL,
                                    description VARCHAR(500),

    -- Schedule details
                                    frequency VARCHAR(30) NOT NULL,
                                    start_date DATE NOT NULL,
                                    end_date DATE,
                                    next_execution_date DATE NOT NULL,
                                    execution_time TIME DEFAULT '09:00:00',

    -- Status
                                    is_active BOOLEAN DEFAULT TRUE,
                                    is_paused BOOLEAN DEFAULT FALSE,

    -- Execution tracking
                                    total_executions INT DEFAULT 0,
                                    successful_executions INT DEFAULT 0,
                                    failed_executions INT DEFAULT 0,
                                    last_executed_at TIMESTAMP,
                                    last_execution_status VARCHAR(30),
                                    last_failure_reason VARCHAR(500),

                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP
);

CREATE INDEX idx_scheduled_payment_customer ON scheduled_payments(customer_id);
CREATE INDEX idx_scheduled_payment_next_exec ON scheduled_payments(next_execution_date);
CREATE INDEX idx_scheduled_payment_active ON scheduled_payments(is_active);

-- Standing Instructions
CREATE TABLE standing_instructions (
                                       id BIGSERIAL PRIMARY KEY,
                                       customer_id BIGINT NOT NULL REFERENCES customers(id),
                                       sender_account_id BIGINT NOT NULL REFERENCES accounts(id),
                                       receiver_account_id BIGINT REFERENCES accounts(id),
                                       beneficiary_id BIGINT REFERENCES beneficiaries(id),

    -- External transfer
                                       receiver_account_number VARCHAR(20),
                                       receiver_ifsc_code VARCHAR(20),
                                       receiver_name VARCHAR(100),

    -- SI details
                                       max_amount NUMERIC(18,2) NOT NULL,
                                       transaction_type VARCHAR(50) NOT NULL,
                                       payment_method VARCHAR(50) NOT NULL,
                                       description VARCHAR(500),

    -- Schedule
                                       frequency VARCHAR(30) NOT NULL,
                                       start_date DATE NOT NULL,
                                       end_date DATE,
                                       next_execution_date DATE NOT NULL,
                                       execution_time TIME DEFAULT '09:00:00',

    -- Status
                                       is_active BOOLEAN DEFAULT TRUE,
                                       is_paused BOOLEAN DEFAULT FALSE,

    -- Execution tracking
                                       total_executions INT DEFAULT 0,
                                       successful_executions INT DEFAULT 0,
                                       failed_executions INT DEFAULT 0,
                                       last_executed_at TIMESTAMP,
                                       last_execution_status VARCHAR(30),
                                       last_failure_reason VARCHAR(500),

                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP
);

CREATE INDEX idx_si_customer ON standing_instructions(customer_id);
CREATE INDEX idx_si_next_exec ON standing_instructions(next_execution_date);

-- Billers
CREATE TABLE billers (
                         id BIGSERIAL PRIMARY KEY,
                         customer_id BIGINT NOT NULL REFERENCES customers(id),
                         biller_name VARCHAR(100) NOT NULL,
                         biller_category VARCHAR(50) NOT NULL,
                         account_number VARCHAR(100) NOT NULL,
                         nickname VARCHAR(100),
                         auto_pay_enabled BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_biller_customer ON billers(customer_id);

-- Bill Payments
CREATE TABLE bill_payments (
                               id BIGSERIAL PRIMARY KEY,
                               transaction_id BIGINT NOT NULL UNIQUE REFERENCES transactions(id) ON DELETE CASCADE,
                               biller_id BIGINT REFERENCES billers(id),
                               bill_category VARCHAR(50) NOT NULL,
                               bill_number VARCHAR(100),
                               due_date DATE,
                               late_fee NUMERIC(10,2) DEFAULT 0,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bill_payment_biller ON bill_payments(biller_id);
CREATE INDEX idx_bill_payment_category ON bill_payments(bill_category);

-- Merchant Categories
CREATE TABLE merchant_categories (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE,
                                     description VARCHAR(200),
                                     icon VARCHAR(100),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Merchants
CREATE TABLE merchants (
                           id BIGSERIAL PRIMARY KEY,
                           merchant_code VARCHAR(50) NOT NULL UNIQUE,
                           merchant_name VARCHAR(200) NOT NULL,
                           category_id BIGINT REFERENCES merchant_categories(id),
                           upi_id VARCHAR(100),
                           is_verified BOOLEAN DEFAULT FALSE,
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_merchant_code ON merchants(merchant_code);
CREATE INDEX idx_merchant_upi ON merchants(upi_id);

-- Merchant Payments
CREATE TABLE merchant_payments (
                                   id BIGSERIAL PRIMARY KEY,
                                   transaction_id BIGINT NOT NULL UNIQUE REFERENCES transactions(id) ON DELETE CASCADE,
                                   merchant_id BIGINT NOT NULL REFERENCES merchants(id),
                                   merchant_reference_id VARCHAR(100),
                                   cashback_amount NUMERIC(10,2) DEFAULT 0,
                                   reward_points INT DEFAULT 0,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_merchant_payment_merchant ON merchant_payments(merchant_id);

-- Transaction Receipts
CREATE TABLE transaction_receipts (
                                      id BIGSERIAL PRIMARY KEY,
                                      transaction_id BIGINT NOT NULL UNIQUE REFERENCES transactions(id) ON DELETE CASCADE,
                                      receipt_number VARCHAR(50) NOT NULL UNIQUE,
                                      receipt_data TEXT NOT NULL,
                                      qr_code TEXT,
                                      generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Disputes
CREATE TABLE transaction_disputes (
                                      id BIGSERIAL PRIMARY KEY,
                                      transaction_id BIGINT NOT NULL REFERENCES transactions(id),
                                      customer_id BIGINT NOT NULL REFERENCES customers(id),
                                      dispute_reason VARCHAR(500) NOT NULL,
                                      status VARCHAR(30) NOT NULL DEFAULT 'RAISED',
                                      resolution TEXT,
                                      resolved_by BIGINT REFERENCES users(id),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      resolved_at TIMESTAMP
);

CREATE INDEX idx_dispute_transaction ON transaction_disputes(transaction_id);
CREATE INDEX idx_dispute_customer ON transaction_disputes(customer_id);
CREATE INDEX idx_dispute_status ON transaction_disputes(status);

-- Fraud Detection Logs
CREATE TABLE fraud_detection_logs (
                                      id BIGSERIAL PRIMARY KEY,
                                      transaction_id BIGINT REFERENCES transactions(id),
                                      customer_id BIGINT REFERENCES customers(id),
                                      rule_triggered VARCHAR(100) NOT NULL,
                                      risk_score NUMERIC(5,2) NOT NULL,
                                      risk_level VARCHAR(20) NOT NULL,
                                      details TEXT,
                                      action_taken VARCHAR(100),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fraud_log_transaction ON fraud_detection_logs(transaction_id);
CREATE INDEX idx_fraud_log_customer ON fraud_detection_logs(customer_id);
CREATE INDEX idx_fraud_log_risk ON fraud_detection_logs(risk_level);

-- Transaction Approvals (for large transactions)
CREATE TABLE transaction_approvals (
                                       id BIGSERIAL PRIMARY KEY,
                                       transaction_id BIGINT NOT NULL UNIQUE REFERENCES transactions(id),
                                       requires_approval BOOLEAN DEFAULT TRUE,
                                       approved BOOLEAN DEFAULT FALSE,
                                       approved_by BIGINT REFERENCES users(id),
                                       rejection_reason VARCHAR(500),
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       approved_at TIMESTAMP
);

CREATE INDEX idx_txn_approval_status ON transaction_approvals(approved);

-- Daily Reconciliation
CREATE TABLE daily_reconciliation (
                                      id BIGSERIAL PRIMARY KEY,
                                      reconciliation_date DATE NOT NULL UNIQUE,
                                      total_transactions BIGINT NOT NULL,
                                      total_debits NUMERIC(18,2) NOT NULL,
                                      total_credits NUMERIC(18,2) NOT NULL,
                                      opening_balance NUMERIC(18,2) NOT NULL,
                                      closing_balance NUMERIC(18,2) NOT NULL,
                                      calculated_balance NUMERIC(18,2) NOT NULL,
                                      discrepancy NUMERIC(18,2) NOT NULL,
                                      is_balanced BOOLEAN NOT NULL,
                                      reconciled_by BIGINT REFERENCES users(id),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Fee Configuration (Admin Editable)
CREATE TABLE transaction_fee_config (
                                        id BIGSERIAL PRIMARY KEY,
                                        transaction_type VARCHAR(50) NOT NULL UNIQUE,
                                        base_fee NUMERIC(10,2) NOT NULL DEFAULT 0,
                                        gst_percentage NUMERIC(5,2) NOT NULL DEFAULT 18,
                                        min_amount NUMERIC(18,2),
                                        max_amount NUMERIC(18,2),
                                        is_active BOOLEAN DEFAULT TRUE,
                                        updated_by BIGINT REFERENCES users(id),
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP
);

-- Insert default transaction categories
INSERT INTO transaction_categories (name, description, icon) VALUES
                                                                 ('TRANSFER', 'Money Transfer', 'transfer'),
                                                                 ('UPI', 'UPI Payment', 'upi'),
                                                                 ('BILL_PAYMENT', 'Bill Payment', 'bill'),
                                                                 ('RECHARGE', 'Mobile/DTH Recharge', 'recharge'),
                                                                 ('MERCHANT', 'Merchant Payment', 'shopping'),
                                                                 ('REFUND', 'Refund', 'refund'),
                                                                 ('INTEREST', 'Interest Credit', 'interest'),
                                                                 ('CHARGES', 'Bank Charges', 'charges'),
                                                                 ('CASHBACK', 'Cashback', 'cashback'),
                                                                 ('SALARY', 'Salary Credit', 'salary');

-- Insert merchant categories
INSERT INTO merchant_categories (name, description, icon) VALUES
                                                              ('GROCERY', 'Grocery & Supermarkets', 'grocery'),
                                                              ('FUEL', 'Fuel Stations', 'fuel'),
                                                              ('UTILITIES', 'Utility Bills', 'utilities'),
                                                              ('FOOD', 'Restaurants & Food', 'food'),
                                                              ('SHOPPING', 'Shopping & Retail', 'shopping'),
                                                              ('TRAVEL', 'Travel & Transport', 'travel'),
                                                              ('ENTERTAINMENT', 'Entertainment', 'entertainment'),
                                                              ('HEALTHCARE', 'Healthcare', 'healthcare'),
                                                              ('EDUCATION', 'Education', 'education'),
                                                              ('OTHERS', 'Others', 'others');

-- Insert default banks
INSERT INTO banks (bank_code, bank_name, ifsc_prefix, is_active) VALUES
                                                                     ('DVIN', 'DVein Bank', 'BANK', true),
                                                                     ('HDFC', 'HDFC Bank', 'HDFC', true),
                                                                     ('ICIC', 'ICICI Bank', 'ICIC', true),
                                                                     ('SBI', 'State Bank of India', 'SBIN', true),
                                                                     ('AXIS', 'Axis Bank', 'UTIB', true),
                                                                     ('KOTAK', 'Kotak Mahindra Bank', 'KKBK', true),
                                                                     ('YES', 'YES Bank', 'YESB', true),
                                                                     ('IDFC', 'IDFC First Bank', 'IDFB', true),
                                                                     ('PNB', 'Punjab National Bank', 'PUNB', true),
                                                                     ('BOB', 'Bank of Baroda', 'BARB', true);

-- Insert default transaction fee config
INSERT INTO transaction_fee_config (transaction_type, base_fee, gst_percentage, min_amount, max_amount) VALUES
                                                                                                            ('IMPS', 5.00, 18.00, 1, 200000),
                                                                                                            ('NEFT', 0.00, 0.00, 1, 1000000),
                                                                                                            ('RTGS', 30.00, 18.00, 200000, NULL),
                                                                                                            ('UPI', 0.00, 0.00, 1, 100000),
                                                                                                            ('INTERNAL_TRANSFER', 0.00, 0.00, 1, NULL);

-- Insert 100 dummy merchants
INSERT INTO merchants (merchant_code, merchant_name, category_id, upi_id, is_verified, is_active) VALUES
-- Grocery (10)
('MER001', 'BigBazaar Supermarket', 1, 'bigbazaar@dveinbank', true, true),
('MER002', 'DMart Hypermarket', 1, 'dmart@dveinbank', true, true),
('MER003', 'Reliance Fresh', 1, 'reliancefresh@dveinbank', true, true),
('MER004', 'More Megastore', 1, 'more@dveinbank', true, true),
('MER005', 'Spencer Retail', 1, 'spencer@dveinbank', true, true),
('MER006', 'Star Bazaar', 1, 'starbazaar@dveinbank', true, true),
('MER007', 'HyperCity', 1, 'hypercity@dveinbank', true, true),
('MER008', 'Nature Basket', 1, 'naturebasket@dveinbank', true, true),
('MER009', 'Foodhall', 1, 'foodhall@dveinbank', true, true),
('MER010', 'Metro Cash & Carry', 1, 'metro@dveinbank', true, true),

-- Fuel (10)
('MER011', 'Indian Oil Petrol Pump', 2, 'indianoil@dveinbank', true, true),
('MER012', 'HP Gas Station', 2, 'hp@dveinbank', true, true),
('MER013', 'Bharat Petroleum', 2, 'bharat@dveinbank', true, true),
('MER014', 'Shell Fuel Station', 2, 'shell@dveinbank', true, true),
('MER015', 'Reliance Petrol Pump', 2, 'reliancefuel@dveinbank', true, true),
('MER016', 'Essar Petrol', 2, 'essar@dveinbank', true, true),
('MER017', 'Nayara Energy', 2, 'nayara@dveinbank', true, true),
('MER018', 'RIL CNG Station', 2, 'rilcng@dveinbank', true, true),
('MER019', 'Adani Gas', 2, 'adanigas@dveinbank', true, true),
('MER020', 'Gulf Oil', 2, 'gulf@dveinbank', true, true),

-- Food (20)
('MER021', 'McDonald''s', 4, 'mcdonalds@dveinbank', true, true),
('MER022', 'KFC', 4, 'kfc@dveinbank', true, true),
('MER023', 'Domino''s Pizza', 4, 'dominos@dveinbank', true, true),
('MER024', 'Pizza Hut', 4, 'pizzahut@dveinbank', true, true),
('MER025', 'Burger King', 4, 'burgerking@dveinbank', true, true),
('MER026', 'Subway', 4, 'subway@dveinbank', true, true),
('MER027', 'Cafe Coffee Day', 4, 'ccd@dveinbank', true, true),
('MER028', 'Starbucks', 4, 'starbucks@dveinbank', true, true),
('MER029', 'Barista', 4, 'barista@dveinbank', true, true),
('MER030', 'The Beer Cafe', 4, 'beercafe@dveinbank', true, true),
('MER031', 'Barbeque Nation', 4, 'barbeque@dveinbank', true, true),
('MER032', 'Mainland China', 4, 'mainlandchina@dveinbank', true, true),
('MER033', 'Haldiram''s', 4, 'haldiram@dveinbank', true, true),
('MER034', 'Biryani Blues', 4, 'biryaniblues@dveinbank', true, true),
('MER035', 'Paradise Biryani', 4, 'paradise@dveinbank', true, true),
('MER036', 'Swiggy', 4, 'swiggy@dveinbank', true, true),
('MER037', 'Zomato', 4, 'zomato@dveinbank', true, true),
('MER038', 'Uber Eats', 4, 'ubereats@dveinbank', true, true),
('MER039', 'Faasos', 4, 'faasos@dveinbank', true, true),
('MER040', 'Box8', 4, 'box8@dveinbank', true, true),

-- Shopping (20)
('MER041', 'Amazon India', 5, 'amazon@dveinbank', true, true),
('MER042', 'Flipkart', 5, 'flipkart@dveinbank', true, true),
('MER043', 'Myntra', 5, 'myntra@dveinbank', true, true),
('MER044', 'Ajio', 5, 'ajio@dveinbank', true, true),
('MER045', 'Shoppers Stop', 5, 'shoppersstop@dveinbank', true, true),
('MER046', 'Lifestyle', 5, 'lifestyle@dveinbank', true, true),
('MER047', 'Pantaloons', 5, 'pantaloons@dveinbank', true, true),
('MER048', 'Westside', 5, 'westside@dveinbank', true, true),
('MER049', 'Max Fashion', 5, 'max@dveinbank', true, true),
('MER050', 'Reliance Trends', 5, 'trends@dveinbank', true, true),
('MER051', 'Titan', 5, 'titan@dveinbank', true, true),
('MER052', 'Tanishq', 5, 'tanishq@dveinbank', true, true),
('MER053', 'Malabar Gold', 5, 'malabar@dveinbank', true, true),
('MER054', 'Croma', 5, 'croma@dveinbank', true, true),
('MER055', 'Vijay Sales', 5, 'vijaysales@dveinbank', true, true),
('MER056', 'Reliance Digital', 5, 'reliancedigital@dveinbank', true, true),
('MER057', 'Samsung', 5, 'samsung@dveinbank', true, true),
('MER058', 'Apple Store', 5, 'apple@dveinbank', true, true),
('MER059', 'HP World', 5, 'hp@dveinbank', true, true),
('MER060', 'Dell Exclusive', 5, 'dell@dveinbank', true, true),

-- Travel (15)
('MER061', 'MakeMyTrip', 6, 'makemytrip@dveinbank', true, true),
('MER062', 'Cleartrip', 6, 'cleartrip@dveinbank', true, true),
('MER063', 'Yatra', 6, 'yatra@dveinbank', true, true),
('MER064', 'Goibibo', 6, 'goibibo@dveinbank', true, true),
('MER065', 'Ixigo', 6, 'ixigo@dveinbank', true, true),
('MER066', 'Ola Cabs', 6, 'ola@dveinbank', true, true),
('MER067', 'Uber', 6, 'uber@dveinbank', true, true),
('MER068', 'Rapido', 6, 'rapido@dveinbank', true, true),
('MER069', 'Redbus', 6, 'redbus@dveinbank', true, true),
('MER070', 'IRCTC', 6, 'irctc@dveinbank', true, true),
('MER071', 'Indigo Airlines', 6, 'indigo@dveinbank', true, true),
('MER072', 'SpiceJet', 6, 'spicejet@dveinbank', true, true),
('MER073', 'Air India', 6, 'airindia@dveinbank', true, true),
('MER074', 'Vistara', 6, 'vistara@dveinbank', true, true),
('MER075', 'OYO Rooms', 6, 'oyo@dveinbank', true, true),

-- Entertainment (10)
('MER076', 'BookMyShow', 7, 'bookmyshow@dveinbank', true, true),
('MER077', 'PVR Cinemas', 7, 'pvr@dveinbank', true, true),
('MER078', 'INOX', 7, 'inox@dveinbank', true, true),
('MER079', 'Netflix', 7, 'netflix@dveinbank', true, true),
('MER080', 'Amazon Prime', 7, 'prime@dveinbank', true, true),
('MER081', 'Hotstar', 7, 'hotstar@dveinbank', true, true),
('MER082', 'Zee5', 7, 'zee5@dveinbank', true, true),
('MER083', 'Sony Liv', 7, 'sonyliv@dveinbank', true, true),
('MER084', 'Spotify', 7, 'spotify@dveinbank', true, true),
('MER085', 'Gaana', 7, 'gaana@dveinbank', true, true),

-- Healthcare (10)
('MER086', 'Apollo Pharmacy', 8, 'apollo@dveinbank', true, true),
('MER087', 'MedPlus', 8, 'medplus@dveinbank', true, true),
('MER088', '1mg', 8, 'onemg@dveinbank', true, true),
('MER089', 'PharmEasy', 8, 'pharmeasy@dveinbank', true, true),
('MER090', 'Netmeds', 8, 'netmeds@dveinbank', true, true),
('MER091', 'Practo', 8, 'practo@dveinbank', true, true),
('MER092', 'Apollo Hospitals', 8, 'apollohospital@dveinbank', true, true),
('MER093', 'Fortis Healthcare', 8, 'fortis@dveinbank', true, true),
('MER094', 'Max Healthcare', 8, 'max@dveinbank', true, true),
('MER095', 'Manipal Hospitals', 8, 'manipal@dveinbank', true, true),

-- Education (5)
('MER096', 'Byju''s', 9, 'byjus@dveinbank', true, true),
('MER097', 'Unacademy', 9, 'unacademy@dveinbank', true, true),
('MER098', 'Vedantu', 9, 'vedantu@dveinbank', true, true),
('MER099', 'Coursera', 9, 'coursera@dveinbank', true, true),
('MER100', 'Udemy', 9, 'udemy@dveinbank', true, true);