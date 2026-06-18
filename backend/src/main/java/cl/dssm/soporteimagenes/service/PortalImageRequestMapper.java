package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.PortalImageRequestResponseDto;
import cl.dssm.soporteimagenes.dto.PublicRequestStatusDto;
import cl.dssm.soporteimagenes.dto.RequestLogDto;
import cl.dssm.soporteimagenes.dto.RequestAttachmentDto;
import cl.dssm.soporteimagenes.entity.PortalImageRequest;
import cl.dssm.soporteimagenes.entity.PortalImageRequestLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PortalImageRequestMapper {
    private final RequestAttachmentService attachmentService;

    public PortalImageRequestResponseDto toDto(PortalImageRequest entity) {
        List<RequestAttachmentDto> attachments = entity.getAttachments() == null
                ? List.of()
                : entity.getAttachments().stream().map(attachmentService::toDto).toList();
        return new PortalImageRequestResponseDto(
                entity.getId(),
                entity.getFolio(),
                entity.getPortalType(),
                entity.getSupportPortal() == null ? null : entity.getSupportPortal().getId(),
                entity.getSupportPortal() == null ? null : entity.getSupportPortal().getCode(),
                portalName(entity),
                entity.getFullName(),
                entity.getRut(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getFixedPhone(),
                entity.getDifficultyType(),
                entity.getPortalTopic() == null ? null : entity.getPortalTopic().getId(),
                entity.getPortalTopic() == null ? null : entity.getPortalTopic().getCode(),
                topicName(entity),
                entity.getOtherDetail(),
                entity.getUserObservation(),
                entity.isConsentAccepted(),
                entity.getSource(),
                entity.getStatus(),
                entity.getInternalObservation(),
                entity.getPublicResponse(),
                entity.getAcknowledgementSentAt(),
                entity.getResponseSentAt(),
                entity.getLastNotificationError(),
                entity.getAssignedUser() == null ? null : entity.getAssignedUser().getId(),
                entity.getAssignedUser() == null ? null : entity.getAssignedUser().getFullName(),
                entity.getResolvedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                attachments.size(),
                attachments
        );
    }

    public PublicRequestStatusDto toPublicStatusDto(PortalImageRequest entity) {
        return new PublicRequestStatusDto(
                entity.getFolio(),
                entity.getPortalType(),
                entity.getSupportPortal() == null ? null : entity.getSupportPortal().getId(),
                portalName(entity),
                entity.getStatus(),
                entity.getPublicResponse(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getResolvedAt()
        );
    }

    public RequestLogDto toLogDto(PortalImageRequestLog log) {
        return new RequestLogDto(
                log.getId(),
                log.getAction(),
                log.getPreviousStatus(),
                log.getNewStatus(),
                log.getObservation(),
                log.getUser() == null ? "Sistema" : log.getUser().getFullName(),
                log.getCreatedAt()
        );
    }

    private String portalName(PortalImageRequest entity) {
        if (entity.getSupportPortal() != null) {
            return entity.getSupportPortal().getName();
        }
        return entity.getPortalType() == null ? "Portal de Imágenes" : entity.getPortalType().getDisplayName();
    }

    private String topicName(PortalImageRequest entity) {
        if (entity.getPortalTopic() != null) {
            return entity.getPortalTopic().getName();
        }
        return entity.getDifficultyType() == null ? null : entity.getDifficultyType().name();
    }
}
