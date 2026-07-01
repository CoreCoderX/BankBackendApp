ALTER TABLE report_requests
    ADD COLUMN account_id BIGINT;

ALTER TABLE report_requests
    ADD CONSTRAINT fk_report_account
        FOREIGN KEY (account_id)
            REFERENCES accounts(id);