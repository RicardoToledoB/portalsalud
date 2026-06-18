package cl.dssm.soporteimagenes.controller;

import cl.dssm.soporteimagenes.dto.PortalTopicDto;
import cl.dssm.soporteimagenes.dto.SupportPortalDto;
import cl.dssm.soporteimagenes.dto.UpsertPortalTopicDto;
import cl.dssm.soporteimagenes.dto.UpsertSupportPortalDto;
import cl.dssm.soporteimagenes.service.PortalAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortalAdminController {
    private final PortalAdminService service;

    @GetMapping("/api/public/portales")
    public List<SupportPortalDto> activePortals() {
        return service.listPortals(true);
    }

    @GetMapping("/api/public/portales/{portalId}/tematicas")
    public List<PortalTopicDto> activeTopics(@PathVariable Long portalId) {
        return service.listTopics(portalId, true);
    }

    @GetMapping("/api/admin/portales")
    public List<SupportPortalDto> portals() {
        return service.listPortals(false);
    }

    @PostMapping("/api/admin/portales")
    public SupportPortalDto createPortal(@Valid @RequestBody UpsertSupportPortalDto dto) {
        return service.createPortal(dto);
    }

    @PutMapping("/api/admin/portales/{portalId}")
    public SupportPortalDto updatePortal(@PathVariable Long portalId, @Valid @RequestBody UpsertSupportPortalDto dto) {
        return service.updatePortal(portalId, dto);
    }

    @GetMapping("/api/admin/portales/{portalId}/tematicas")
    public List<PortalTopicDto> topics(@PathVariable Long portalId) {
        return service.listTopics(portalId, false);
    }

    @PostMapping("/api/admin/portales/{portalId}/tematicas")
    public PortalTopicDto createTopic(@PathVariable Long portalId, @Valid @RequestBody UpsertPortalTopicDto dto) {
        return service.createTopic(portalId, dto);
    }

    @PutMapping("/api/admin/portales/{portalId}/tematicas/{topicId}")
    public PortalTopicDto updateTopic(@PathVariable Long portalId, @PathVariable Long topicId, @Valid @RequestBody UpsertPortalTopicDto dto) {
        return service.updateTopic(portalId, topicId, dto);
    }
}
