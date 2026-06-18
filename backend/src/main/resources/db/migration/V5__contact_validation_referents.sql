ALTER TABLE portal_image_requests
    ADD COLUMN fixed_phone VARCHAR(50) NULL AFTER phone;

ALTER TABLE users
    ADD COLUMN portal_type VARCHAR(80) NULL AFTER role,
    ADD COLUMN difficulty_type VARCHAR(80) NULL AFTER portal_type;

UPDATE users
SET portal_type = 'PORTAL_IMAGENES'
WHERE role = 'REFERENTE_DSSM' AND portal_type IS NULL;
