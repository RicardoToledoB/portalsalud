# Soporte Portales Salud DSSM - v4.2

## Cambios incorporados

- Se agrega configuración por portal para permitir o deshabilitar la caja de observación abierta del formulario público.
- Para el Portal de Imágenes queda deshabilitada la observación abierta por defecto.
- Se actualizan las alternativas visibles del Portal de Imágenes:
  1. Portal no reconoce mi usuario y/o contraseña.
  2. Debo actualizar mis datos de contacto.
  3. No he registrado un correo electrónico para asociar al Portal.
  4. No he recibido información para tareas como compartir mis estudios en mi correo.
  5. No he recibido en mi correo la tarea para recuperar mi contraseña.
  6. Soy tutor o responsable de una persona que no tiene accesos al Portal.
- Se agrega configuración por temática para exigir datos de tutor/responsable.
- La opción de tutor/responsable despliega campos estructurados: nombre, RUT, teléfono, correo opcional y relación con la persona usuaria.
- Se mantiene la carga opcional de imágenes.
- Se agregan los datos de tutor/responsable al detalle de solicitud, CSV y Excel.
- Se agrega migración Flyway V10.

## Migración incluida

Archivo:

```text
backend/src/main/resources/db/migration/V10__closed_topics_tutor_observation_settings.sql
```

Al reiniciar el backend con Flyway activo, la migración se ejecuta automáticamente.
