# Ajuste visual de tablas y dashboard

Versión: v3.8

Cambios aplicados:

- Se corrigió el quiebre visual de tarjetas del dashboard al 100% de zoom.
- Las tarjetas de indicadores ahora se ordenan de forma balanceada según el ancho disponible.
- Las tablas del backoffice ahora tienen desplazamiento horizontal controlado cuando el ancho no alcanza.
- La columna de acciones queda fija al costado derecho para que los botones no desaparezcan.
- Se ajustaron tablas de:
  - Solicitudes
  - Usuarios
  - Portales y temáticas
- Se agregó scrollbar horizontal estilizado en contenedores de tabla.

Validación realizada:

```bash
npm install --legacy-peer-deps --no-audit --no-fund
npm run build
```

Resultado: build Angular ejecutado correctamente.
