package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.PortalType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePortalImageRequestDto(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 20) String rut,
        PortalType portalType,
        Long portalId,
        @Email @Size(max = 150) String email,
        @Size(max = 50) String phone,
        @Size(max = 50) String fixedPhone,
        DifficultyType difficultyType,
        Long topicId,
        @Size(max = 500) String otherDetail,
        @Size(max = 1000) String userObservation,
        @Size(max = 150) String tutorFullName,
        @Size(max = 20) String tutorRut,
        @Email @Size(max = 150) String tutorEmail,
        @Size(max = 50) String tutorPhone,
        @Size(max = 100) String tutorRelationship,
        @AssertTrue boolean consentAccepted,
        @NotBlank @Size(max = 80) String captchaId,
        @NotBlank @Size(max = 10) String captchaAnswer,
        @Size(max = 120) String website
) {}
