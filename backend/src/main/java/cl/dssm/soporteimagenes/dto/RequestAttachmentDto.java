package cl.dssm.soporteimagenes.dto;

import java.time.LocalDateTime;

public record RequestAttachmentDto(
        Long id,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        String downloadUrl,
        LocalDateTime createdAt
) {}
