# Ajuste final de tablas al 100% de zoom

Se ajustaron las vistas del backoffice para evitar que las tablas se corten o escondan botones cuando el navegador está al 100% de zoom.

## Vistas ajustadas

- Solicitudes
- Usuarios internos
- Portales y temáticas

## Cambios principales

- Se eliminaron columnas sticky que provocaban solapamientos visuales.
- Se ajustaron anchos de columnas con `table-layout: fixed`.
- Se permitió corte controlado de textos largos y chips.
- Se redujo el ancho mínimo de tablas.
- Se agregaron reglas responsivas para ocultar columnas secundarias cuando el ancho no alcanza.
- En Portales, el formulario y tabla pasan a una columna antes para evitar que la tabla quede comprimida.

## Notas

- La información crítica se mantiene visible.
- En pantallas más pequeñas, la tabla mantiene scroll horizontal controlado.
- Los botones de acción quedan visibles dentro del contenedor.
