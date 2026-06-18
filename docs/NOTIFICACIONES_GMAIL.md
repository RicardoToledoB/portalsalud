# Notificaciones por Gmail - Soporte Portales Salud DSSM

Cuenta configurada para pruebas:

```text
portalesssm.notifica@gmail.com
```

## Importante de seguridad

No se debe usar la contraseña normal de Gmail. Se debe usar una contraseña de aplicación de Google, sin espacios.

Si la contraseña de aplicación fue compartida por chat, correo o capturas, revóquela y genere una nueva antes de pasar a producción.

## Sin export en el servidor

El proyecto queda preparado para funcionar sin variables de entorno. La configuración se edita en archivos YAML.

### Local

Editar:

```text
backend/src/main/resources/application.yml
```

Cambiar:

```yaml
spring:
  mail:
    password: CAMBIAR_CLAVE_APP_GMAIL_SIN_ESPACIOS
```

Luego ejecutar:

```bash
cd backend
mvn spring-boot:run
```

### Público / servidor

Editar:

```text
backend/src/main/resources/application-public.yml
```

Cambiar al menos:

```yaml
spring:
  datasource:
    password: CAMBIAR_PASSWORD_BD_PRODUCCION
  mail:
    password: CAMBIAR_CLAVE_APP_GMAIL_SIN_ESPACIOS

app:
  security:
    jwt-secret: CAMBIAR-JWT-SECRET-LARGO-PRODUCCION-PORTALES-SALUD-DSSM-2026
```

Luego ejecutar:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=public
```

o como JAR:

```bash
java -jar soporte-portales-salud-0.0.1-SNAPSHOT.jar --spring.profiles.active=public
```

## Flujo de correos implementado

1. Al crear solicitud pública:
   - Correo al solicitante con folio y enlace de seguimiento.
   - Correo al o los referentes del portal correspondiente.

2. Al responder solicitud desde backoffice:
   - Correo al solicitante con la respuesta visible y enlace de seguimiento.

3. Registro interno:
   - Se registra en historial si el correo fue enviado.
   - Se registra error si Gmail o SMTP rechaza el envío.

## Dominios configurados

Frontend público:

```text
https://soporteportales.dssm.cl
```

Backend público:

```text
https://soporteportales-api.dssm.cl/api
```
