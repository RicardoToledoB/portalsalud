package cl.dssm.soporteimagenes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertPortalTopicDto(
        @NotBlank @Size(max = 80) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull Boolean active,
        Boolean requiresDetail,
        Integer displayOrder
) {}
