package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.RequestStatus;
import cl.dssm.soporteimagenes.enums.PortalType;

import java.time.LocalDateTime;
import java.util.List;

public record PortalImageRequestResponseDto(
        Long id,
        String folio,
        PortalType portalType,
        Long portalId,
        String portalCode,
        String portalName,
        String fullName,
        String rut,
        String email,
        String phone,
        String fixedPhone,
        DifficultyType difficultyType,
        Long topicId,
        String topicCode,
        String topicName,
        String otherDetail,
        String userObservation,
        boolean consentAccepted,
        String source,
        RequestStatus status,
        String internalObservation,
        String publicResponse,
        LocalDateTime acknowledgementSentAt,
        LocalDateTime responseSentAt,
        String lastNotificationError,
        Long assignedUserId,
        String assignedUserName,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int attachmentCount,
        List<RequestAttachmentDto> attachments
) {}
