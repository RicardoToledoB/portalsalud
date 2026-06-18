package cl.dssm.soporteimagenes.dto;

import java.time.LocalDateTime;

public record PortalTopicDto(
        Long id,
        Long portalId,
        String portalName,
        String code,
        String name,
        String description,
        boolean active,
        boolean requiresDetail,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
