# Soporte Portales Salud DSSM v4.3 - Frontend tutor local 8888

Esta versión incluye el bloque visual de datos del tutor/responsable en el formulario público.

## Cambios clave

- `frontend/src/environments/environment.ts` apunta a `http://localhost:8888/api`.
- El formulario público muestra campos de tutor cuando la temática trae `requiresTutorContact: true`.
- La caja de observación abierta se oculta cuando el portal trae `allowUserObservation: false`.
- Se mantiene adjuntar imágenes opcionales.

## Ejecutar frontend local

```bash
cd frontend
npm install --legacy-peer-deps --no-audit --no-fund
npm start
```

Abrir:

```text
http://localhost:4200/solicitud
```

## Backend esperado

```text
http://localhost:8888/api
```

Validar backend:

```bash
curl http://localhost:8888/actuator/health
```

## Prueba funcional

1. Seleccionar Portal de Imágenes.
2. Seleccionar: `Soy tutor o responsable de una persona que no tiene accesos al Portal.`
3. Deben aparecer los campos del tutor/responsable antes de adjuntar imágenes.
