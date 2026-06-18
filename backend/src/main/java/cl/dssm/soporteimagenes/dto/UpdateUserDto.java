package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateUserDto(
        @Size(max = 150) String fullName,
        @Email @Size(max = 150) String email,
        @Size(min = 8, max = 100) String password,
        UserRole role,
        Boolean active,
        PortalType portalType,
        Long portalId,
        Long topicId,
        List<UserPortalAssignmentInputDto> portalAssignments,
        DifficultyType difficultyType
) {}
