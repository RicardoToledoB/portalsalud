package cl.dssm.soporteimagenes.controller;

import cl.dssm.soporteimagenes.dto.*;
import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.RequestStatus;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.service.PortalImageRequestService;
import cl.dssm.soporteimagenes.service.RequestAttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/solicitudes", "/api/portal-imagenes/solicitudes"})
@RequiredArgsConstructor
public class PortalImageRequestController {
    private final PortalImageRequestService service;
    private final RequestAttachmentService attachmentService;

    @GetMapping
    public Page<PortalImageRequestResponseDto> search(
            @RequestParam(required = false) PortalType portalType,
            @RequestParam(required = false) Long portalId,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) DifficultyType difficultyType,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String rut,
            @RequestParam(required = false) String folio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        return service.search(portalType, portalId, status, difficultyType, topicId, rut, folio, from, to, pageable);
    }

    @GetMapping("/{id}")
    public PortalImageRequestResponseDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/logs")
    public List<RequestLogDto> logs(@PathVariable Long id) {
        return service.getLogs(id);
    }

    @PatchMapping("/{id}/estado")
    public PortalImageRequestResponseDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateRequestStatusDto dto) {
        return service.updateStatus(id, dto);
    }

    @PatchMapping("/{id}/observacion")
    public PortalImageRequestResponseDto updateInternalObservation(@PathVariable Long id, @Valid @RequestBody UpdateInternalObservationDto dto) {
        return service.updateInternalObservation(id, dto);
    }

    @GetMapping("/{id}/adjuntos/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        service.getById(id);
        RequestAttachmentService.AttachmentFile file = attachmentService.download(id, attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.originalFilename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .body(file.resource());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) PortalType portalType,
            @RequestParam(required = false) Long portalId,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) DifficultyType difficultyType,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csv = service.exportCsv(portalType, portalId, status, difficultyType, topicId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=solicitudes-portales-salud.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) PortalType portalType,
            @RequestParam(required = false) Long portalId,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) DifficultyType difficultyType,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] xlsx = service.exportExcel(portalType, portalId, status, difficultyType, topicId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=solicitudes-portales-salud.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

}
