# Despliegue público sin usar export

## Backend

El perfil público usa:

```text
backend/src/main/resources/application-public.yml
```

No requiere `export`. Edite directamente ese archivo en el servidor.

Valores principales:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/soporte_portales_salud?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago
    username: soporte_salud
    password: CAMBIAR_PASSWORD_BD_PRODUCCION
  mail:
    host: smtp.gmail.com
    port: 587
    username: portalesssm.notifica@gmail.com
    password: CAMBIAR_CLAVE_APP_GMAIL_SIN_ESPACIOS

app:
  portal:
    public-base-url: https://soporteportales.dssm.cl
    backoffice-base-url: https://soporteportales.dssm.cl
  cors:
    allowed-origins: https://soporteportales.dssm.cl,http://localhost:4200
```

Ejecutar:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=public
```

## Frontend

Producción apunta a:

```text
https://soporteportales-api.dssm.cl/api
```

Archivo:

```text
frontend/src/environments/environment.prod.ts
```

Build:

```bash
cd frontend
npm install --legacy-peer-deps --no-audit --no-fund
npm run build
```

## Nginx sugerido

### Frontend `soporteportales.dssm.cl`

```nginx
server {
    listen 80;
    server_name soporteportales.dssm.cl;

    root /var/www/soporteportales/browser;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### Backend `soporteportales-api.dssm.cl`

```nginx
server {
    listen 80;
    server_name soporteportales-api.dssm.cl;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Para HTTPS, agregar certificados institucionales o Let's Encrypt según la política DSSM.
