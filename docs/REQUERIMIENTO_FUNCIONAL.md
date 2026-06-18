# Requerimiento funcional - Formulario Soporte Portales Salud DSSM

## Objetivo

Implementar una solución web institucional que permita registrar, gestionar y resolver solicitudes de usuarios que presenten dificultades para acceder a Portales Salud DSSM.

## Flujo usuario

1. Usuario escanea código QR.
2. Ingresa al formulario público.
3. Selecciona portal o sistema. Por ahora queda habilitado por defecto Portal de Imágenes.
4. Registra datos de identificación y contacto.
5. Selecciona el tipo de dificultad.
6. Acepta el uso de datos para gestión de la solicitud.
7. Sistema genera folio.
8. Si el usuario informó correo y el SMTP está habilitado, recibe correo de recepción con número de requerimiento.
9. Usuario conserva folio para seguimiento.
10. Usuario puede consultar el estado ingresando folio y RUT en el módulo de seguimiento público.

## Flujo funcionario

1. Referente DSSM ingresa al backoffice.
2. Revisa la bandeja de solicitudes.
3. Filtra por fecha, estado, RUT, folio o tipo de dificultad.
4. Contacta al usuario o actualiza antecedentes en plataforma correspondiente.
5. Registra observación interna.
6. Registra respuesta pública para el solicitante.
7. Cambia el estado de la solicitud.
8. Opcionalmente envía correo de respuesta al solicitante.
9. Sistema registra trazabilidad de cambios y notificaciones.

## Perfiles

### Administrador

- Crear usuarios internos.
- Editar usuarios internos.
- Activar/desactivar usuarios.
- Asignar perfiles.
- Ver todas las solicitudes.
- Exportar registros.
- Revisar dashboard.

### Referente DSSM

- Ver solicitudes.
- Filtrar solicitudes.
- Revisar detalle.
- Registrar gestión.
- Cambiar estado.
- Exportar registros si se mantiene habilitado por política interna.

## Campos públicos

- Portal o sistema. Valor inicial habilitado: Portal de Imágenes.
- Nombre completo.
- RUT.
- Correo electrónico.
- Número de contacto.
- Tipo de dificultad:
  - Mi contraseña no funciona.
  - Mis datos de contacto no están actualizados.
  - No he registrado un correo electrónico.
  - Otro.
- Detalle si selecciona Otro.
- Observación adicional.
- Consentimiento simple para gestión de solicitud.

## Campos internos

- Folio.
- Portal o sistema asociado.
- Estado.
- Funcionario asignado.
- Observación interna.
- Fecha de ingreso.
- Fecha de actualización.
- Fecha de resolución.
- Respuesta pública para el solicitante.
- Fecha de envío de correo de recepción.
- Fecha de envío de correo de respuesta.
- Último error de notificación, si corresponde.
- Trazabilidad de cambios y notificaciones.

## Texto informativo sugerido

La información ingresada será utilizada exclusivamente para gestionar dificultades de acceso a Portales Salud DSSM y actualizar antecedentes de contacto asociados al usuario. No ingrese información clínica, diagnósticos ni antecedentes médicos en este formulario.


## Seguimiento público

El seguimiento público debe solicitar dos datos:

- Número de requerimiento o folio.
- RUT del solicitante.

El sistema muestra solo información administrativa del caso: estado, fecha de ingreso, fecha de actualización, fecha de cierre y respuesta pública. No debe exponer observaciones internas ni datos clínicos.

## Notificaciones por correo

- Al registrar una solicitud, el sistema puede enviar correo de recepción con el folio.
- Al guardar una gestión, el funcionario puede marcar la opción de enviar respuesta al solicitante.
- El correo de respuesta incluye folio, estado, respuesta pública y enlace de seguimiento.
- Si el solicitante no informó correo o el SMTP no está habilitado, la solicitud sigue siendo gestionable y consultable por folio/RUT.

## Mantenedores administrativos

El sistema incorpora mantenedores para que el administrador pueda escalar la plataforma sin modificar código:

### Portales
Permite crear, editar, activar o desactivar portales/sistemas disponibles para el formulario público. Campos principales: código, nombre, descripción, estado y orden.

### Temáticas por portal
Cada portal posee sus propias temáticas. Las temáticas activas se muestran en el formulario según el portal seleccionado. Para Portal de Imágenes se incluyen por defecto: contraseña no funciona, datos de contacto no actualizados, sin correo registrado y otro.

### Referentes
El referente DSSM se asigna a un portal completo. La temática clasifica el requerimiento, pero no determina por sí sola al responsable.

## Actualización v3.5 - edición de usuarios y múltiples portales por referente

- El administrador puede editar usuarios existentes.
- El administrador puede cambiar el perfil, correo, nombre y portales asignados.
- Un referente DSSM puede quedar asociado a uno o más portales.
- Si el referente tiene un solo portal, opcionalmente puede restringirse a una temática específica.
- Si el referente tiene más de un portal, visualiza todas las temáticas de los portales asignados.
- Los mantenedores de Portales y Usuarios siguen siendo exclusivos de administradores.
- Los referentes solo visualizan Dashboard y Solicitudes, filtradas por sus portales asignados.
