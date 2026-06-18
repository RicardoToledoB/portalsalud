CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL
);

CREATE TABLE portal_image_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    rut VARCHAR(20) NOT NULL,
    email VARCHAR(150) NULL,
    phone VARCHAR(50) NULL,
    difficulty_type VARCHAR(80) NOT NULL,
    other_detail VARCHAR(500) NULL,
    user_observation VARCHAR(1000) NULL,
    consent_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
    internal_observation VARCHAR(1500) NULL,
    assigned_user_id BIGINT NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT fk_request_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users(id)
);

CREATE INDEX idx_portal_image_requests_folio ON portal_image_requests(folio);
CREATE INDEX idx_portal_image_requests_rut ON portal_image_requests(rut);
CREATE INDEX idx_portal_image_requests_status ON portal_image_requests(status);
CREATE INDEX idx_portal_image_requests_created_at ON portal_image_requests(created_at);
CREATE INDEX idx_portal_image_requests_difficulty ON portal_image_requests(difficulty_type);

CREATE TABLE portal_image_request_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    previous_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NULL,
    observation VARCHAR(1500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_request FOREIGN KEY (request_id) REFERENCES portal_image_requests(id),
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_request_logs_request_id ON portal_image_request_logs(request_id);
CREATE INDEX idx_request_logs_created_at ON portal_image_request_logs(created_at);
