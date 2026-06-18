CREATE TABLE request_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    relative_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_request_attachments_request
        FOREIGN KEY (request_id) REFERENCES portal_image_requests(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_request_attachments_request_id ON request_attachments(request_id);
