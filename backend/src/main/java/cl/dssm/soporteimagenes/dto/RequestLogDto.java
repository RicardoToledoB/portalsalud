package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.LogAction;
import cl.dssm.soporteimagenes.enums.RequestStatus;

import java.time.LocalDateTime;

public record RequestLogDto(
        Long id,
        LogAction action,
        RequestStatus previousStatus,
        RequestStatus newStatus,
        String observation,
        String userName,
        LocalDateTime createdAt
) {}
