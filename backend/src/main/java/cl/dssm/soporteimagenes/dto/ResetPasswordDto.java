package cl.dssm.soporteimagenes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
        @NotBlank @Size(min = 8, max = 100) String password
) {}
