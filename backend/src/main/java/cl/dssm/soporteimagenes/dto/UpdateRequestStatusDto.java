package cl.dssm.soporteimagenes.dto;

import cl.dssm.soporteimagenes.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequestStatusDto(
        @NotNull RequestStatus status,
        @Size(max = 1500) String observation,
        @Size(max = 1500) String publicResponse,
        Boolean notifyRequester,
        Long assignedUserId
) {}
