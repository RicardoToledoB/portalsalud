package cl.dssm.soporteimagenes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateInternalObservationDto(
        @NotBlank @Size(max = 1500) String internalObservation
) {}
