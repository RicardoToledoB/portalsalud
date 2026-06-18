ALTER TABLE portal_image_requests
    ADD COLUMN source VARCHAR(50) NOT NULL DEFAULT 'QR_FORM' AFTER consent_accepted;
