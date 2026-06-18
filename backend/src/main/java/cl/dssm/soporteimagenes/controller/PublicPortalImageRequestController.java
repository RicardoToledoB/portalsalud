package cl.dssm.soporteimagenes.controller;

import cl.dssm.soporteimagenes.dto.CaptchaChallengeDto;
import cl.dssm.soporteimagenes.dto.CreatePortalImageRequestDto;
import cl.dssm.soporteimagenes.dto.PortalImageRequestResponseDto;
import cl.dssm.soporteimagenes.dto.PublicRequestStatusDto;
import cl.dssm.soporteimagenes.service.CaptchaService;
import cl.dssm.soporteimagenes.service.PortalImageRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/public/solicitudes", "/api/public/portal-imagenes/solicitudes"})
@RequiredArgsConstructor
public class PublicPortalImageRequestController {
    private final PortalImageRequestService service;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public CaptchaChallengeDto captcha() {
        return captchaService.createChallenge();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public PortalImageRequestResponseDto create(@Valid @RequestBody CreatePortalImageRequestDto dto) {
        return service.createPublicRequest(dto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PortalImageRequestResponseDto createWithAttachments(
            @Valid @RequestPart("data") CreatePortalImageRequestDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return service.createPublicRequest(dto, files == null ? List.of() : files);
    }

    @GetMapping("/seguimiento")
    public PublicRequestStatusDto status(@RequestParam String folio, @RequestParam String rut) {
        return service.getPublicStatus(folio, rut);
    }

    @GetMapping("/folio/{folio}")
    public PublicRequestStatusDto statusByFolio(@PathVariable String folio, @RequestParam String rut) {
        return service.getPublicStatus(folio, rut);
    }
}
