# Guía breve de despliegue

## Backend

Compilar:

```bash
cd backend
mvn clean package -DskipTests
```

Ejecutar:

```bash
java -jar target/soporte-portales-salud-0.0.1-SNAPSHOT.jar
```

Con perfil productivo:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/soporte-portales-salud-0.0.1-SNAPSHOT.jar
```

## Frontend

Compilar:

```bash
cd frontend
npm install
npm run build
```

Publicar el contenido generado en:

```text
frontend/dist/soporte-portales-salud-front/browser
```

## Nginx sugerido

```nginx
server {
    listen 80;
    server_name portalesalud.dssm.cl;

    root /var/www/soporte-portales-salud/browser;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Variables de correo y seguimiento

Para habilitar notificaciones automáticas por correo:

```bash
PUBLIC_BASE_URL=https://portalesalud.dssm.cl
MAIL_ENABLED=true
SMTP_HOST=smtp.institucional.cl
SMTP_PORT=587
SMTP_USER=usuario_smtp
SMTP_PASSWORD=clave_smtp
SMTP_AUTH=true
SMTP_STARTTLS=true
MAIL_FROM=no-reply@saludmagallanes.cl
MAIL_REPLY_TO=soporte@saludmagallanes.cl
```

Si `MAIL_ENABLED=false`, el sistema no envía correos, pero mantiene el folio, el seguimiento público por folio/RUT y la trazabilidad interna.

## Ajustes v2 antes de producción

1. Cambiar la clave de base de datos para que sea exclusiva de este proyecto.
2. Configurar SMTP institucional y probar envío real de correos.
3. Validar que la URL pública `PUBLIC_BASE_URL` apunte al dominio definitivo.
4. Crear referentes DSSM y asignarlos al portal/temática correspondiente.
5. Revisar que el formulario público no solicite ni permita antecedentes clínicos.
6. Validar descarga Excel desde `/api/solicitudes/export/excel`.

## Configuración de adjuntos de imágenes

Para activar almacenamiento persistente de imágenes adjuntas en producción, configurar una carpeta fuera del proyecto:

```bash
sudo mkdir -p /var/lib/soporte-portales-salud/uploads
sudo chown -R soporte:soporte /var/lib/soporte-portales-salud/uploads
```

Variables recomendadas:

```bash
export UPLOAD_DIR=/var/lib/soporte-portales-salud/uploads
export UPLOAD_MAX_FILES=5
export UPLOAD_MAX_FILE_SIZE_BYTES=5242880
export MAX_FILE_SIZE=5MB
export MAX_REQUEST_SIZE=25MB
```

Los archivos permitidos son imágenes JPG, PNG y WEBP. El acceso a descarga de adjuntos requiere autenticación del backoffice.

## Migración v3.5

Esta versión agrega la tabla `user_portal_assignments` mediante Flyway:

```text
V9__user_multiple_portal_assignments.sql
```

La migración copia automáticamente la asignación histórica desde `users.support_portal_id` y `users.portal_topic_id` hacia la nueva tabla. No es necesario crear tablas manualmente.
