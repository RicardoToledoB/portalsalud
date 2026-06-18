package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateUserDto(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull UserRole role,
        PortalType portalType,
        Long portalId,
        Long topicId,
        List<UserPortalAssignmentInputDto> portalAssignments,
        DifficultyType difficultyType
) {}
