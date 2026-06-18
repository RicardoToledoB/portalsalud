package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.PortalTopicDto;
import cl.dssm.soporteimagenes.dto.SupportPortalDto;
import cl.dssm.soporteimagenes.dto.UpsertPortalTopicDto;
import cl.dssm.soporteimagenes.dto.UpsertSupportPortalDto;
import cl.dssm.soporteimagenes.entity.PortalTopic;
import cl.dssm.soporteimagenes.entity.SupportPortal;
import cl.dssm.soporteimagenes.repository.PortalTopicRepository;
import cl.dssm.soporteimagenes.repository.SupportPortalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PortalAdminService {
    private final SupportPortalRepository portalRepository;
    private final PortalTopicRepository topicRepository;

    @Transactional(readOnly = true)
    public List<SupportPortalDto> listPortals(boolean onlyActive) {
        List<SupportPortal> portals = onlyActive
                ? portalRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                : portalRepository.findAllByOrderByDisplayOrderAscNameAsc();
        return portals.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SupportPortalDto getPortal(Long id) {
        return toDto(findPortal(id));
    }

    @Transactional
    public SupportPortalDto createPortal(UpsertSupportPortalDto dto) {
        String code = normalizeCode(dto.code());
        if (portalRepository.existsByCode(code)) {
            throw new ResponseStatusException(BAD_REQUEST, "Ya existe un portal con ese código");
        }
        SupportPortal portal = SupportPortal.builder()
                .code(code)
                .name(dto.name().trim())
                .description(blankToNull(dto.description()))
                .active(Boolean.TRUE.equals(dto.active()))
                .displayOrder(dto.displayOrder() == null ? 0 : dto.displayOrder())
                .build();
        return toDto(portalRepository.save(portal));
    }

    @Transactional
    public SupportPortalDto updatePortal(Long id, UpsertSupportPortalDto dto) {
        SupportPortal portal = findPortal(id);
        String code = normalizeCode(dto.code());
        portalRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ResponseStatusException(BAD_REQUEST, "Ya existe otro portal con ese código");
            }
        });
        portal.setCode(code);
        portal.setName(dto.name().trim());
        portal.setDescription(blankToNull(dto.description()));
        portal.setActive(Boolean.TRUE.equals(dto.active()));
        portal.setDisplayOrder(dto.displayOrder() == null ? 0 : dto.displayOrder());
        return toDto(portalRepository.save(portal));
    }

    @Transactional(readOnly = true)
    public List<PortalTopicDto> listTopics(Long portalId, boolean onlyActive) {
        findPortal(portalId);
        List<PortalTopic> topics = onlyActive
                ? topicRepository.findByPortal_IdAndActiveTrueOrderByDisplayOrderAscNameAsc(portalId)
                : topicRepository.findByPortal_IdOrderByDisplayOrderAscNameAsc(portalId);
        return topics.stream().map(this::toDto).toList();
    }

    @Transactional
    public PortalTopicDto createTopic(Long portalId, UpsertPortalTopicDto dto) {
        SupportPortal portal = findPortal(portalId);
        String code = normalizeCode(dto.code());
        if (topicRepository.existsByPortalAndCode(portal, code)) {
            throw new ResponseStatusException(BAD_REQUEST, "Ya existe una temática con ese código para este portal");
        }
        PortalTopic topic = PortalTopic.builder()
                .portal(portal)
                .code(code)
                .name(dto.name().trim())
                .description(blankToNull(dto.description()))
                .active(Boolean.TRUE.equals(dto.active()))
                .requiresDetail(Boolean.TRUE.equals(dto.requiresDetail()))
                .displayOrder(dto.displayOrder() == null ? 0 : dto.displayOrder())
                .build();
        return toDto(topicRepository.save(topic));
    }

    @Transactional
    public PortalTopicDto updateTopic(Long portalId, Long topicId, UpsertPortalTopicDto dto) {
        SupportPortal portal = findPortal(portalId);
        PortalTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Temática no encontrada"));
        if (!topic.getPortal().getId().equals(portal.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "La temática no pertenece al portal indicado");
        }
        String code = normalizeCode(dto.code());
        topicRepository.findByPortal_IdAndCode(portalId, code).ifPresent(existing -> {
            if (!existing.getId().equals(topicId)) {
                throw new ResponseStatusException(BAD_REQUEST, "Ya existe otra temática con ese código para este portal");
            }
        });
        topic.setCode(code);
        topic.setName(dto.name().trim());
        topic.setDescription(blankToNull(dto.description()));
        topic.setActive(Boolean.TRUE.equals(dto.active()));
        topic.setRequiresDetail(Boolean.TRUE.equals(dto.requiresDetail()));
        topic.setDisplayOrder(dto.displayOrder() == null ? 0 : dto.displayOrder());
        return toDto(topicRepository.save(topic));
    }

    private SupportPortal findPortal(Long id) {
        return portalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Portal no encontrado"));
    }

    private SupportPortalDto toDto(SupportPortal portal) {
        long topicCount = portal.getTopics() == null ? 0 : portal.getTopics().size();
        return new SupportPortalDto(
                portal.getId(),
                portal.getCode(),
                portal.getName(),
                portal.getDescription(),
                portal.isActive(),
                portal.getDisplayOrder(),
                topicCount,
                portal.getCreatedAt(),
                portal.getUpdatedAt()
        );
    }

    private PortalTopicDto toDto(PortalTopic topic) {
        return new PortalTopicDto(
                topic.getId(),
                topic.getPortal().getId(),
                topic.getPortal().getName(),
                topic.getCode(),
                topic.getName(),
                topic.getDescription(),
                topic.isActive(),
                topic.isRequiresDetail(),
                topic.getDisplayOrder(),
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase().replaceAll("[^A-Z0-9_]+", "_");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
