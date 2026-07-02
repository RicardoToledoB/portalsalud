ALTER TABLE support_portals
    ADD COLUMN allow_user_observation BOOLEAN NOT NULL DEFAULT TRUE AFTER display_order;

ALTER TABLE portal_topics
    ADD COLUMN requires_tutor_contact BOOLEAN NOT NULL DEFAULT FALSE AFTER requires_detail;

ALTER TABLE portal_image_requests
    ADD COLUMN tutor_full_name VARCHAR(150) NULL AFTER user_observation,
    ADD COLUMN tutor_rut VARCHAR(20) NULL AFTER tutor_full_name,
    ADD COLUMN tutor_email VARCHAR(150) NULL AFTER tutor_rut,
    ADD COLUMN tutor_phone VARCHAR(50) NULL AFTER tutor_email,
    ADD COLUMN tutor_relationship VARCHAR(100) NULL AFTER tutor_phone;

SET @portal_imagenes_id = (SELECT id FROM support_portals WHERE code = 'PORTAL_IMAGENES' LIMIT 1);

UPDATE support_portals
SET allow_user_observation = FALSE
WHERE code = 'PORTAL_IMAGENES';

UPDATE portal_topics
SET name = 'Portal no reconoce mi usuario y/o contraseña.',
    description = 'Dificultad de acceso por usuario o contraseña no reconocidos.',
    active = TRUE,
    requires_detail = FALSE,
    requires_tutor_contact = FALSE,
    display_order = 1
WHERE portal_id = @portal_imagenes_id AND code = 'CONTRASENA_NO_FUNCIONA';

UPDATE portal_topics
SET name = 'Debo actualizar mis datos de contacto.',
    description = 'Actualización de correo, celular o teléfono de contacto asociado al Portal.',
    active = TRUE,
    requires_detail = FALSE,
    requires_tutor_contact = FALSE,
    display_order = 2
WHERE portal_id = @portal_imagenes_id AND code = 'DATOS_CONTACTO_NO_ACTUALIZADOS';

UPDATE portal_topics
SET name = 'No he registrado un correo electrónico para asociar al Portal.',
    description = 'Usuario sin correo electrónico registrado para asociación o recuperación de acceso.',
    active = TRUE,
    requires_detail = FALSE,
    requires_tutor_contact = FALSE,
    display_order = 3
WHERE portal_id = @portal_imagenes_id AND code = 'SIN_CORREO_REGISTRADO';

INSERT INTO portal_topics (portal_id, code, name, description, active, requires_detail, requires_tutor_contact, display_order)
SELECT @portal_imagenes_id, 'NO_RECIBI_COMPARTIR_ESTUDIOS',
       'No he recibido información para tareas como compartir mis estudios en mi correo.',
       'El usuario no ha recibido instrucciones o información para compartir estudios desde el Portal.',
       TRUE, FALSE, FALSE, 4
WHERE @portal_imagenes_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM portal_topics WHERE portal_id = @portal_imagenes_id AND code = 'NO_RECIBI_COMPARTIR_ESTUDIOS');

INSERT INTO portal_topics (portal_id, code, name, description, active, requires_detail, requires_tutor_contact, display_order)
SELECT @portal_imagenes_id, 'NO_RECIBI_RECUPERAR_CONTRASENA',
       'No he recibido en mi correo la tarea para recuperar mi contraseña.',
       'El usuario no ha recibido correo o tarea para recuperación de contraseña.',
       TRUE, FALSE, FALSE, 5
WHERE @portal_imagenes_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM portal_topics WHERE portal_id = @portal_imagenes_id AND code = 'NO_RECIBI_RECUPERAR_CONTRASENA');

INSERT INTO portal_topics (portal_id, code, name, description, active, requires_detail, requires_tutor_contact, display_order)
SELECT @portal_imagenes_id, 'TUTOR_RESPONSABLE_SIN_ACCESO',
       'Soy tutor o responsable de una persona que no tiene accesos al Portal.',
       'Solicitud ingresada por tutor o responsable; requiere datos de contacto del tutor.',
       TRUE, FALSE, TRUE, 6
WHERE @portal_imagenes_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM portal_topics WHERE portal_id = @portal_imagenes_id AND code = 'TUTOR_RESPONSABLE_SIN_ACCESO');

UPDATE portal_topics
SET active = FALSE,
    requires_detail = FALSE,
    requires_tutor_contact = FALSE,
    display_order = 99
WHERE portal_id = @portal_imagenes_id AND code = 'OTRO';
