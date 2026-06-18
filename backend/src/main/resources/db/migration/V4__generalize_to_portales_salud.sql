ALTER TABLE portal_image_requests
    ADD COLUMN portal_type VARCHAR(80) NOT NULL DEFAULT 'PORTAL_IMAGENES' AFTER folio;

CREATE INDEX idx_portal_image_requests_portal_type ON portal_image_requests(portal_type);
