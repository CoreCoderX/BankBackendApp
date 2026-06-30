-- V1__initial_schema.sql - PostgreSQL Compatible Version

-- Users Table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       phone VARCHAR(15) UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       is_active BOOLEAN DEFAULT FALSE,
                       email_verified BOOLEAN DEFAULT FALSE,
                       is_locked BOOLEAN DEFAULT FALSE,
                       failed_login_attempts INT DEFAULT 0,
                       locked_until TIMESTAMP,
                       password_changed_at TIMESTAMP,
                       totp_enabled BOOLEAN DEFAULT FALSE,
                       biometric_enabled BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       last_login_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);

-- Customers Table
CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
                           first_name VARCHAR(50) NOT NULL,
                           last_name VARCHAR(50) NOT NULL,
                           middle_name VARCHAR(50),
                           date_of_birth DATE,
                           address VARCHAR(200),
                           city VARCHAR(50),
                           state VARCHAR(50),
                           postal_code VARCHAR(10),
                           country VARCHAR(50),
                           pan VARCHAR(20) UNIQUE,
                           aadhaar VARCHAR(20) UNIQUE,
                           profile_photo_url VARCHAR(255),
                           status VARCHAR(20) DEFAULT 'ACTIVE',
                           suspension_reason VARCHAR(500),
                           suspended_at TIMESTAMP,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP
);

CREATE INDEX idx_customers_user_id ON customers(user_id);
CREATE INDEX idx_customers_pan ON customers(pan);
CREATE INDEX idx_customers_aadhaar ON customers(aadhaar);

-- Accounts Table
CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          customer_id BIGINT NOT NULL REFERENCES customers(id),
                          account_number VARCHAR(20) NOT NULL UNIQUE,
                          account_type VARCHAR(20) NOT NULL,
                          ifsc_code VARCHAR(20) NOT NULL,
                          branch_code VARCHAR(10) NOT NULL,
                          branch_name VARCHAR(50),
                          balance NUMERIC(15,2) DEFAULT 0,
                          minimum_balance NUMERIC(15,2) DEFAULT 0,
                          status VARCHAR(20) DEFAULT 'ACTIVE',
                          is_primary BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          closed_at TIMESTAMP,
                          closure_reason VARCHAR(200)
);

CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_status ON accounts(status);

-- OTPs Table
CREATE TABLE otps (
                      id BIGSERIAL PRIMARY KEY,
                      email VARCHAR(100) NOT NULL,
                      code VARCHAR(6) NOT NULL,
                      otp_type VARCHAR(30) NOT NULL,
                      expires_at TIMESTAMP NOT NULL,
                      verified BOOLEAN DEFAULT FALSE,
                      retry_count INT DEFAULT 0,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      verified_at TIMESTAMP
);

CREATE INDEX idx_otps_email_type ON otps(email, otp_type);

-- TOTP Secrets Table
CREATE TABLE totp_secrets (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
                              secret VARCHAR(255) NOT NULL UNIQUE,
                              is_enabled BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              enabled_at TIMESTAMP,
                              last_used_at TIMESTAMP
);

-- MPIN Table
CREATE TABLE mpins (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
                       pin_hash VARCHAR(255) NOT NULL,
                       failed_attempts INT DEFAULT 0,
                       is_locked BOOLEAN DEFAULT FALSE,
                       locked_until TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       last_used_at TIMESTAMP
);

-- Devices Table
CREATE TABLE devices (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL REFERENCES users(id),
                         device_id VARCHAR(255) NOT NULL UNIQUE,
                         device_name VARCHAR(255) NOT NULL,
                         device_fingerprint VARCHAR(500),
                         user_agent VARCHAR(500),
                         ip_address VARCHAR(100),
                         is_trusted BOOLEAN DEFAULT FALSE,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP,
                         last_used_at TIMESTAMP
);

CREATE INDEX idx_devices_user_device ON devices(user_id, device_id);

-- Sessions Table
CREATE TABLE sessions (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL REFERENCES users(id),
                          refresh_token VARCHAR(500) NOT NULL UNIQUE,
                          device_id BIGINT REFERENCES devices(id),
                          ip_address VARCHAR(100),
                          user_agent VARCHAR(500),
                          expires_at TIMESTAMP NOT NULL,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          last_activity_at TIMESTAMP
);

CREATE INDEX idx_sessions_user_active ON sessions(user_id, is_active);
CREATE INDEX idx_sessions_token ON sessions(refresh_token);

-- Login History Table
CREATE TABLE login_history (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL REFERENCES users(id),
                               ip_address VARCHAR(100),
                               user_agent VARCHAR(500),
                               location VARCHAR(100),
                               device_type VARCHAR(50),
                               successful BOOLEAN DEFAULT TRUE,
                               failure_reason VARCHAR(200),
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_history_user_created ON login_history(user_id, created_at);

-- Beneficiaries Table
CREATE TABLE beneficiaries (
                               id BIGSERIAL PRIMARY KEY,
                               account_id BIGINT NOT NULL REFERENCES accounts(id),
                               beneficiary_name VARCHAR(100) NOT NULL,
                               beneficiary_account_number VARCHAR(20) NOT NULL,
                               ifsc_code VARCHAR(20) NOT NULL,
                               bank_name VARCHAR(100),
                               is_verified BOOLEAN DEFAULT FALSE,
                               remarks VARCHAR(500),
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_beneficiaries_account_id ON beneficiaries(account_id);
CREATE INDEX idx_beneficiaries_account_number ON beneficiaries(beneficiary_account_number);

-- Nominees Table
CREATE TABLE nominees (
                          id BIGSERIAL PRIMARY KEY,
                          account_id BIGINT NOT NULL REFERENCES accounts(id),
                          nominee_name VARCHAR(100) NOT NULL,
                          nominee_date_of_birth DATE,
                          nominee_relationship VARCHAR(100),
                          nominee_phone VARCHAR(20),
                          nominee_email VARCHAR(100),
                          nominee_address VARCHAR(200),
                          percentage NUMERIC(5,2) DEFAULT 100,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP
);

CREATE INDEX idx_nominees_account_id ON nominees(account_id);

-- Documents Table
CREATE TABLE documents (
                           id BIGSERIAL PRIMARY KEY,
                           customer_id BIGINT NOT NULL REFERENCES customers(id),
                           document_type VARCHAR(50) NOT NULL,
                           document_number VARCHAR(200) NOT NULL,
                           document_url VARCHAR(500) NOT NULL,
                           expiry_date DATE,
                           issue_date DATE,
                           issuing_authority VARCHAR(100),
                           is_verified BOOLEAN DEFAULT FALSE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_customer_id ON documents(customer_id);
CREATE INDEX idx_documents_document_type ON documents(document_type);

-- KYC Records Table
CREATE TABLE kyc_records (
                             id BIGSERIAL PRIMARY KEY,
                             customer_id BIGINT NOT NULL UNIQUE REFERENCES customers(id),
                             status VARCHAR(20) DEFAULT 'PENDING',
                             rejection_reason VARCHAR(200),
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP,
                             submitted_at TIMESTAMP,
                             approved_at TIMESTAMP,
                             rejected_at TIMESTAMP,
                             expiry_date DATE,
                             approved_by VARCHAR(100)
);

CREATE INDEX idx_kyc_customer_id ON kyc_records(customer_id);
CREATE INDEX idx_kyc_status ON kyc_records(status);

-- Consents Table
CREATE TABLE consents (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL REFERENCES users(id),
                          consent_type VARCHAR(100) NOT NULL,
                          consent_version VARCHAR(50) NOT NULL,
                          is_accepted BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          accepted_at TIMESTAMP,
                          ip_address VARCHAR(200)
);

CREATE INDEX idx_consents_user_id ON consents(user_id);
CREATE INDEX idx_consents_consent_type ON consents(consent_type);

-- Debit Cards Table
CREATE TABLE debit_cards (
                             id BIGSERIAL PRIMARY KEY,
                             account_id BIGINT NOT NULL REFERENCES accounts(id),
                             card_number VARCHAR(16) NOT NULL UNIQUE,
                             card_holder_name VARCHAR(100) NOT NULL,
                             cvv VARCHAR(4) NOT NULL,
                             expiry_date DATE NOT NULL,
                             pin VARCHAR(4),
                             pin_hash VARCHAR(255),
                             status VARCHAR(20) DEFAULT 'INACTIVE',
                             international_enabled BOOLEAN DEFAULT FALSE,
                             online_transaction_enabled BOOLEAN DEFAULT TRUE,
                             atm_withdrawal_enabled BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP,
                             activated_at TIMESTAMP,
                             blocked_at TIMESTAMP,
                             block_reason VARCHAR(200)
);

CREATE INDEX idx_debit_cards_account_id ON debit_cards(account_id);
CREATE INDEX idx_debit_cards_card_number ON debit_cards(card_number);

-- Credit Cards Table
CREATE TABLE credit_cards (
                              id BIGSERIAL PRIMARY KEY,
                              account_id BIGINT NOT NULL REFERENCES accounts(id),
                              card_number VARCHAR(16),
                              card_holder_name VARCHAR(100) NOT NULL,
                              cvv VARCHAR(4),
                              expiry_date DATE,
                              pin VARCHAR(4),
                              pin_hash VARCHAR(255),
                              credit_limit NUMERIC(18,2) NOT NULL,
                              available_credit NUMERIC(18,2) DEFAULT 0,
                              outstanding_balance NUMERIC(18,2) DEFAULT 0,
                              interest_rate NUMERIC(6,2) DEFAULT 18.5,
                              status VARCHAR(20) DEFAULT 'INACTIVE',
                              is_approved BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,
                              approved_at TIMESTAMP,
                              activated_at TIMESTAMP,
                              blocked_at TIMESTAMP,
                              block_reason VARCHAR(200),
                              rejection_reason VARCHAR(200),
                              rejected_at TIMESTAMP,
                              billing_due_date DATE
);

CREATE INDEX idx_credit_cards_account_id ON credit_cards(account_id);
CREATE INDEX idx_credit_cards_card_number ON credit_cards(card_number);
CREATE INDEX idx_credit_cards_status ON credit_cards(status);

-- Card Security Settings Table
CREATE TABLE card_security_settings (
                                        id BIGSERIAL PRIMARY KEY,
                                        card_id BIGINT NOT NULL UNIQUE,
                                        card_type VARCHAR(20) NOT NULL,
                                        international_transaction_allowed BOOLEAN DEFAULT FALSE,
                                        online_transaction_allowed BOOLEAN DEFAULT TRUE,
                                        atm_withdrawal_allowed BOOLEAN DEFAULT TRUE,
                                        contactless_payment_allowed BOOLEAN DEFAULT TRUE,
                                        daily_withdrawal_limit NUMERIC(15,2),
                                        daily_transaction_limit NUMERIC(15,2),
                                        monthly_transaction_limit NUMERIC(15,2),
                                        allowed_countries VARCHAR(200),
                                        blocked_countries VARCHAR(200),
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP
);

-- Roles Table
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(200),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Permissions Table
CREATE TABLE permissions (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(200),
                             module VARCHAR(50) NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions Table
CREATE TABLE role_permissions (
                                  id BIGSERIAL PRIMARY KEY,
                                  role_id BIGINT NOT NULL REFERENCES roles(id),
                                  permission_id BIGINT NOT NULL REFERENCES permissions(id),
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit Logs Table
CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            action VARCHAR(50) NOT NULL,
                            entity_type VARCHAR(100),
                            entity_id BIGINT,
                            description VARCHAR(500),
                            ip_address VARCHAR(100),
                            user_agent VARCHAR(2000),
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Insert Default Roles
INSERT INTO roles (name, description) VALUES
                                          ('CUSTOMER', 'Customer role'),
                                          ('ADMIN', 'Admin role'),
                                          ('SUPER_ADMIN', 'Super Admin role');

-- Insert Default Permissions
INSERT INTO permissions (name, description, module) VALUES
                                                        ('CREATE_ACCOUNT', 'Create account', 'ACCOUNT'),
                                                        ('VIEW_ACCOUNT', 'View account', 'ACCOUNT'),
                                                        ('UPDATE_ACCOUNT', 'Update account', 'ACCOUNT'),
                                                        ('CLOSE_ACCOUNT', 'Close account', 'ACCOUNT'),
                                                        ('CREATE_CARD', 'Create card', 'CARD'),
                                                        ('BLOCK_CARD', 'Block card', 'CARD'),
                                                        ('APPROVE_KYC', 'Approve KYC', 'KYC'),
                                                        ('REJECT_KYC', 'Reject KYC', 'KYC'),
                                                        ('VIEW_AUDIT_LOG', 'View audit logs', 'AUDIT');

-- Insert Hardcoded Admin User (Password: Admin@123)
-- INSERT INTO users (email, phone, password, role, active, email_verified) VALUES
--     ('admin@banking.com', NULL, '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lYfB8hHBGu1K', 'ADMIN', TRUE, TRUE);