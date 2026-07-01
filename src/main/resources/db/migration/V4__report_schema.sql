CREATE TABLE report_requests (
                                 id VARCHAR(36) PRIMARY KEY,
                                 report_type VARCHAR(50) NOT NULL,
                                 format VARCHAR(20) NOT NULL,
                                 status VARCHAR(20) NOT NULL,
                                 requested_by VARCHAR(36) NOT NULL,
                                 user_role VARCHAR(20) NOT NULL,
                                 start_date DATE,
                                 end_date DATE,
                                 filters LONGTEXT,
                                 file_path VARCHAR(500),
                                 download_url VARCHAR(500),
                                 generated_at TIMESTAMP,
                                 expires_at TIMESTAMP,
                                 download_count INT DEFAULT 0,
                                 error_message LONGTEXT,
                                 created_by VARCHAR(36),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                 INDEX idx_requested_by (requested_by),
                                 INDEX idx_report_type (report_type),
                                 INDEX idx_status (status),
                                 INDEX idx_created_at (created_at)
);

CREATE TABLE report_files (
                              id VARCHAR(36) PRIMARY KEY,
                              report_request_id VARCHAR(36) NOT NULL,
                              file_name VARCHAR(255) NOT NULL,
                              file_size BIGINT NOT NULL,
                              mime_type VARCHAR(100) NOT NULL,
                              storage_path VARCHAR(500) NOT NULL,
                              download_count INT DEFAULT 0,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              deleted_at TIMESTAMP,

                              INDEX idx_report_request_id (report_request_id),
                              INDEX idx_created_at (created_at),
                              FOREIGN KEY (report_request_id) REFERENCES report_requests(id) ON DELETE CASCADE
);