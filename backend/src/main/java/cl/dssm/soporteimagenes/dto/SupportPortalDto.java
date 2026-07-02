package cl.dssm.soporteimagenes.dto;

import java.time.LocalDateTime;

public record SupportPortalDto(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        Integer displayOrder,
        boolean allowUserObservation,
        long topicCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
