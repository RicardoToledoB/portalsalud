package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public record UserDto(
        Long id,
        String fullName,
        String email,
        UserRole role,
        boolean active,
        PortalType portalType,
        Long portalId,
        String portalName,
        Long topicId,
        String topicName,
        List<UserPortalAssignmentDto> portalAssignments,
        DifficultyType difficultyType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
