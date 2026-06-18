# Soporte Portales Salud DSSM

Sistema para ingreso, seguimiento y gestión interna de requerimientos asociados a portales de salud DSSM.

## Componentes

- Backend: Spring Boot 3.3.5 + MySQL + Flyway + JWT + SMTP Gmail.
- Frontend: Angular + Angular Material.
- Público: formulario de solicitud y seguimiento por folio + RUT.
- Backoffice: dashboard, solicitudes, portales/temáticas, usuarios y referentes.

## Local

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install --legacy-peer-deps --no-audit --no-fund
ng serve -o
```

URLs locales:

```text
Formulario público: http://localhost:4200/solicitud
Seguimiento: http://localhost:4200/seguimiento
Backoffice: http://localhost:4200/login
Backend API: http://localhost:8080/api
```

## Público

Frontend:

```text
https://soporteportales.dssm.cl
```

Backend:

```text
https://soporteportales-api.dssm.cl/api
```

El frontend productivo ya apunta a `https://soporteportales-api.dssm.cl/api`.

## Notificaciones

La configuración SMTP está preparada para Gmail:

```text
portalesssm.notifica@gmail.com
```

Por seguridad, la contraseña de aplicación no se deja escrita en el proyecto. Debe pegarse sin espacios en:

```text
backend/src/main/resources/application.yml
backend/src/main/resources/application-public.yml
```

Ver:

```text
docs/NOTIFICACIONES_GMAIL.md
```

## Sin export en servidor

El perfil público usa el archivo:

```text
backend/src/main/resources/application-public.yml
```

Ejecutar con:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=public
```

o como JAR:

```bash
java -jar soporte-portales-salud-0.0.1-SNAPSHOT.jar --spring.profiles.active=public
```

Ver:

```text
docs/DEPLOY_PUBLICO_SIN_EXPORT.md
```
