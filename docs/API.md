# APIs - Soporte Portales Salud DSSM

## Autenticación

### Login

`POST /api/auth/login`

```json
{
  "email": "admin@dssm.cl",
  "password": "Cambiar.2026!"
}
```

Respuesta:

```json
{
  "token": "jwt",
  "userId": 1,
  "fullName": "Administrador Portales Salud",
  "email": "admin@dssm.cl",
  "role": "ADMIN"
}
```

## Solicitudes públicas

### Crear solicitud

`POST /api/public/solicitudes`

```json
{
  "portalType": "PORTAL_IMAGENES",
  "fullName": "Juan Pérez",
  "rut": "12.345.678-5",
  "email": "juan@email.cl",
  "phone": "+56912345678",
  "difficultyType": "CONTRASENA_NO_FUNCIONA",
  "otherDetail": null,
  "userObservation": "No puedo ingresar al portal",
  "consentAccepted": true
}
```

Al crear la solicitud se genera un folio, por ejemplo `REQ-2026-000001`. Si el correo está informado y el SMTP está habilitado, el sistema envía un correo automático de recepción al solicitante con su número de requerimiento y enlace de seguimiento.

### Consultar seguimiento por folio y RUT

`GET /api/public/solicitudes/seguimiento?folio=REQ-2026-000001&rut=12.345.678-5`

También disponible como:

`GET /api/public/solicitudes/folio/REQ-2026-000001?rut=12.345.678-5`

Respuesta:

```json
{
  "folio": "REQ-2026-000001",
  "portalType": "PORTAL_IMAGENES",
  "portalName": "Portal de Imágenes",
  "status": "RESUELTO",
  "publicResponse": "Sus datos fueron actualizados. Puede intentar recuperar su contraseña nuevamente.",
  "createdAt": "2026-06-16T10:15:00",
  "updatedAt": "2026-06-16T11:20:00",
  "resolvedAt": "2026-06-16T11:20:00"
}
```

## Solicitudes internas

Requieren header:

```http
Authorization: Bearer {token}
```

### Listar con filtros

`GET /api/solicitudes?status=PENDIENTE&rut=12345678&page=0&size=20&sort=createdAt,desc`

Filtros disponibles:

- `portalType`
- `status`
- `difficultyType`
- `rut`
- `folio`
- `from` formato `YYYY-MM-DD`
- `to` formato `YYYY-MM-DD`

### Ver detalle

`GET /api/solicitudes/{id}`

### Cambiar estado, registrar respuesta y notificar

`PATCH /api/solicitudes/{id}/estado`

```json
{
  "status": "RESUELTO",
  "observation": "Se actualizó correo de contacto en plataforma correspondiente.",
  "publicResponse": "Sus datos fueron actualizados. Puede intentar recuperar su contraseña nuevamente desde el portal correspondiente.",
  "notifyRequester": true,
  "assignedUserId": 2
}
```

- `observation`: observación interna, visible solo para funcionarios.
- `publicResponse`: respuesta visible para el solicitante en el módulo de seguimiento.
- `notifyRequester`: si es `true`, intenta enviar correo al solicitante.
- Si el solicitante no informó correo, el sistema no envía correo y deja registro de la situación.

### Agregar observación interna

`PATCH /api/solicitudes/{id}/observacion`

```json
{
  "internalObservation": "Usuario contactado telefónicamente. Se instruye recuperación de contraseña."
}
```

### Ver trazabilidad

`GET /api/solicitudes/{id}/logs`

### Exportar CSV

`GET /api/solicitudes/export`

La exportación incluye folio, estado, datos de contacto, observaciones internas, respuesta pública, fecha de envío de correo de recepción, fecha de envío de respuesta y último error de notificación, si existe.

## Usuarios internos

Solo perfil `ADMIN`.

### Listar usuarios

`GET /api/users`

### Crear usuario

`POST /api/users`

```json
{
  "fullName": "Referente Portales Salud",
  "email": "referente@dssm.cl",
  "password": "Temporal.2026!",
  "role": "REFERENTE_DSSM"
}
```

### Actualizar usuario

`PUT /api/users/{id}`

```json
{
  "fullName": "Referente DSSM",
  "role": "REFERENTE_DSSM",
  "active": true
}
```

### Activar / desactivar

`PATCH /api/users/{id}/enable`

`PATCH /api/users/{id}/disable`

## Dashboard

`GET /api/dashboard/summary`

## Adjuntos de imágenes

### Crear solicitud con imágenes

`POST /api/public/solicitudes`

Content-Type: `multipart/form-data`

Partes:

- `data`: JSON con los mismos campos del formulario público.
- `files`: una o más imágenes opcionales.

Restricciones:

- Formatos: JPG, PNG, WEBP.
- Máximo: 5 imágenes.
- Tamaño máximo: 5 MB por imagen.

### Descargar adjunto desde backoffice

`GET /api/solicitudes/{id}/adjuntos/{attachmentId}/download`

Requiere autenticación JWT. El archivo se descarga como adjunto y no queda expuesto públicamente.
