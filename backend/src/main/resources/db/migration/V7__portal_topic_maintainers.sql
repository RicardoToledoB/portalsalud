CREATE TABLE support_portals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL
);

CREATE TABLE portal_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portal_id BIGINT NOT NULL,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    requires_detail BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT fk_portal_topics_portal FOREIGN KEY (portal_id) REFERENCES support_portals(id),
    CONSTRAINT uk_portal_topics_portal_code UNIQUE (portal_id, code)
);

INSERT INTO support_portals (code, name, description, active, display_order)
VALUES ('PORTAL_IMAGENES', 'Portal de Imágenes', 'Soporte de acceso al Portal de Imágenes Magallanes', TRUE, 1);

SET @portal_imagenes_id = (SELECT id FROM support_portals WHERE code = 'PORTAL_IMAGENES' LIMIT 1);

INSERT INTO portal_topics (portal_id, code, name, description, active, requires_detail, display_order)
VALUES
(@portal_imagenes_id, 'CONTRASENA_NO_FUNCIONA', 'Mi contraseña no funciona', 'Dificultades de acceso por contraseña', TRUE, FALSE, 1),
(@portal_imagenes_id, 'DATOS_CONTACTO_NO_ACTUALIZADOS', 'Mis datos de contacto no están actualizados', 'Actualización de correo, celular o teléfono de contacto', TRUE, FALSE, 2),
(@portal_imagenes_id, 'SIN_CORREO_REGISTRADO', 'No he registrado un correo electrónico', 'Usuario sin correo electrónico registrado para recuperación de acceso', TRUE, FALSE, 3),
(@portal_imagenes_id, 'OTRO', 'Otro', 'Otra dificultad no clasificada', TRUE, TRUE, 99);

ALTER TABLE portal_image_requests
    ADD COLUMN support_portal_id BIGINT NULL AFTER portal_type,
    ADD COLUMN portal_topic_id BIGINT NULL AFTER difficulty_type,
    ADD CONSTRAINT fk_requests_support_portal FOREIGN KEY (support_portal_id) REFERENCES support_portals(id),
    ADD CONSTRAINT fk_requests_portal_topic FOREIGN KEY (portal_topic_id) REFERENCES portal_topics(id);

UPDATE portal_image_requests
SET support_portal_id = @portal_imagenes_id
WHERE support_portal_id IS NULL;

UPDATE portal_image_requests r
JOIN portal_topics t ON t.portal_id = @portal_imagenes_id AND t.code = r.difficulty_type
SET r.portal_topic_id = t.id
WHERE r.portal_topic_id IS NULL;

ALTER TABLE users
    ADD COLUMN support_portal_id BIGINT NULL AFTER portal_type,
    ADD CONSTRAINT fk_users_support_portal FOREIGN KEY (support_portal_id) REFERENCES support_portals(id);

UPDATE users
SET support_portal_id = @portal_imagenes_id
WHERE role = 'REFERENTE_DSSM' AND support_portal_id IS NULL;

CREATE INDEX idx_requests_support_portal_id ON portal_image_requests(support_portal_id);
CREATE INDEX idx_requests_portal_topic_id ON portal_image_requests(portal_topic_id);
CREATE INDEX idx_users_support_portal_id ON users(support_portal_id);
CREATE INDEX idx_support_portals_active_order ON support_portals(active, display_order);
CREATE INDEX idx_portal_topics_portal_active_order ON portal_topics(portal_id, active, display_order);
