package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.entity.PortalImageRequest;
import cl.dssm.soporteimagenes.entity.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@saludmagallanes.cl}")
    private String from;

    @Value("${app.mail.from-name:Soporte Portales Salud DSSM}")
    private String fromName;

    @Value("${app.mail.reply-to:}")
    private String replyTo;

    @Value("${app.portal.public-base-url:https://soporteportales.dssm.cl}")
    private String publicBaseUrl;

    @Value("${app.portal.backoffice-base-url:https://soporteportales.dssm.cl}")
    private String backofficeBaseUrl;

    public NotificationResult sendAcknowledgement(PortalImageRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return NotificationResult.skipped("Solicitante sin correo electrónico informado.");
        }
        String portalName = portalName(request);
        String subject = "Solicitud recibida - " + portalName + " - " + request.getFolio();
        String body = "Estimado/a " + safe(request.getFullName()) + ":\n\n"
                + "Hemos recibido su solicitud de apoyo asociada a " + portalName + ".\n\n"
                + "Número de requerimiento: " + request.getFolio() + "\n"
                + "Estado inicial: " + displayStatus(request.getStatus()) + "\n\n"
                + "Puede consultar el estado de su solicitud ingresando su número de requerimiento y RUT en:\n"
                + trackingUrl(request.getFolio()) + "\n\n"
                + "Importante: no envíe antecedentes clínicos, diagnósticos, exámenes ni datos sensibles por correo.\n\n"
                + "Servicio de Salud Magallanes";
        return send(request.getEmail(), subject, body);
    }

    public NotificationResult sendNewRequestToReferent(PortalImageRequest request, User referent) {
        if (referent == null || referent.getEmail() == null || referent.getEmail().isBlank()) {
            return NotificationResult.skipped("Solicitud sin referente asignado o referente sin correo.");
        }
        String portalName = portalName(request);
        String topicName = topicName(request);
        String subject = "Nueva solicitud - " + portalName + " - " + request.getFolio();
        String body = "Estimado/a " + safe(referent.getFullName()) + ":\n\n"
                + "Se ha ingresado una nueva solicitud asociada a uno de sus portales asignados.\n\n"
                + "Folio: " + request.getFolio() + "\n"
                + "Portal: " + portalName + "\n"
                + "Temática: " + topicName + "\n"
                + "Solicitante: " + safe(request.getFullName()) + "\n"
                + "RUT: " + safe(request.getRut()) + "\n"
                + "Estado: " + displayStatus(request.getStatus()) + "\n\n"
                + "Puede revisar y gestionar la solicitud en el backoffice:\n"
                + backofficeUrl() + "\n\n"
                + "Recuerde no solicitar ni enviar contraseñas por correo electrónico.\n\n"
                + "Servicio de Salud Magallanes";
        return send(referent.getEmail(), subject, body);
    }

    public NotificationResult sendResponse(PortalImageRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return NotificationResult.skipped("Solicitante sin correo electrónico informado.");
        }
        String publicResponse = request.getPublicResponse() == null || request.getPublicResponse().isBlank()
                ? "Su solicitud fue actualizada por el equipo responsable."
                : request.getPublicResponse().trim();
        String portalName = portalName(request);
        String subject = "Actualización de solicitud - " + portalName + " - " + request.getFolio();
        String body = "Estimado/a " + safe(request.getFullName()) + ":\n\n"
                + "Su solicitud asociada a " + portalName + " ha sido actualizada.\n\n"
                + "Número de requerimiento: " + request.getFolio() + "\n"
                + "Estado actual: " + displayStatus(request.getStatus()) + "\n\n"
                + "Respuesta del equipo responsable:\n"
                + publicResponse + "\n\n"
                + "También puede revisar el seguimiento ingresando su número de requerimiento y RUT en:\n"
                + trackingUrl(request.getFolio()) + "\n\n"
                + "Importante: este canal es para soporte administrativo de acceso a portales. No incluya antecedentes clínicos, diagnósticos ni exámenes por correo.\n\n"
                + "Servicio de Salud Magallanes";
        return send(request.getEmail(), subject, body);
    }

    private String portalName(PortalImageRequest request) {
        if (request.getSupportPortal() != null) {
            return request.getSupportPortal().getName();
        }
        return request.getPortalType() == null ? "Portal de Imágenes" : request.getPortalType().getDisplayName();
    }

    private String topicName(PortalImageRequest request) {
        if (request.getPortalTopic() != null) {
            return request.getPortalTopic().getName();
        }
        return request.getDifficultyType() == null ? "Sin temática" : displayDifficulty(request.getDifficultyType());
    }

    private String displayStatus(cl.dssm.soporteimagenes.enums.RequestStatus status) {
        if (status == null) return "Sin estado";
        return switch (status) {
            case PENDIENTE -> "Pendiente";
            case EN_REVISION -> "En revisión";
            case CONTACTADO -> "Contactado";
            case RESUELTO -> "Resuelto";
            case NO_CORRESPONDE -> "No corresponde";
        };
    }

    private String displayDifficulty(cl.dssm.soporteimagenes.enums.DifficultyType difficultyType) {
        if (difficultyType == null) return "Sin temática";
        return switch (difficultyType) {
            case CONTRASENA_NO_FUNCIONA -> "Mi contraseña no funciona";
            case DATOS_CONTACTO_NO_ACTUALIZADOS -> "Mis datos de contacto no están actualizados";
            case SIN_CORREO_REGISTRADO -> "No he registrado un correo electrónico";
            case OTRO -> "Otro";
        };
    }

    private NotificationResult send(String to, String subject, String body) {
        if (!mailEnabled) {
            return NotificationResult.skipped("Envío de correo deshabilitado. Revise app.mail.enabled.");
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return NotificationResult.failed("No existe JavaMailSender disponible. Revise configuración SMTP.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(from, fromName));
            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            return NotificationResult.success();
        } catch (Exception ex) {
            return NotificationResult.failed(ex.getMessage());
        }
    }

    private String trackingUrl(String folio) {
        String base = cleanBaseUrl(publicBaseUrl, "https://soporteportales.dssm.cl");
        return base + "/seguimiento?folio=" + folio;
    }

    private String backofficeUrl() {
        return cleanBaseUrl(backofficeBaseUrl, cleanBaseUrl(publicBaseUrl, "https://soporteportales.dssm.cl")) + "/admin/solicitudes";
    }

    private String cleanBaseUrl(String value, String fallback) {
        String base = value == null || value.isBlank() ? fallback : value;
        return base.replaceAll("/+$", "");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "No informado" : value.trim();
    }

    public record NotificationResult(boolean sent, boolean skipped, String message) {
        public static NotificationResult success() {
            return new NotificationResult(true, false, null);
        }

        public static NotificationResult skipped(String message) {
            return new NotificationResult(false, true, message);
        }

        public static NotificationResult failed(String message) {
            return new NotificationResult(false, false, message);
        }
    }
}
