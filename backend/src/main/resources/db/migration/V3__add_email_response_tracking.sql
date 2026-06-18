ALTER TABLE portal_image_requests
    ADD COLUMN public_response VARCHAR(1500) NULL AFTER internal_observation,
    ADD COLUMN acknowledgement_sent_at DATETIME NULL AFTER public_response,
    ADD COLUMN response_sent_at DATETIME NULL AFTER acknowledgement_sent_at,
    ADD COLUMN last_notification_error VARCHAR(1000) NULL AFTER response_sent_at;

CREATE INDEX idx_portal_image_requests_response_sent_at ON portal_image_requests(response_sent_at);
