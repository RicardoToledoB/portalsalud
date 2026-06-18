package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.RequestStatus;
import cl.dssm.soporteimagenes.enums.PortalType;

import java.time.LocalDateTime;

public record PublicRequestStatusDto(
        String folio,
        PortalType portalType,
        Long portalId,
        String portalName,
        RequestStatus status,
        String publicResponse,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt
) {}
