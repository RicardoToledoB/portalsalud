package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.UserRole;

import java.util.List;

public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String email,
        UserRole role,
        Long portalId,
        String portalName,
        Long topicId,
        String topicName,
        List<UserPortalAssignmentDto> portalAssignments,
        DifficultyType difficultyType
) {}
