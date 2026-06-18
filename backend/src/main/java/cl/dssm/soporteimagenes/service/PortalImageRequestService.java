package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.*;
import cl.dssm.soporteimagenes.entity.PortalImageRequest;
import cl.dssm.soporteimagenes.entity.PortalImageRequestLog;
import cl.dssm.soporteimagenes.entity.PortalTopic;
import cl.dssm.soporteimagenes.entity.SupportPortal;
import cl.dssm.soporteimagenes.entity.User;
import cl.dssm.soporteimagenes.entity.UserPortalAssignment;
import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.LogAction;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.enums.RequestStatus;
import cl.dssm.soporteimagenes.enums.UserRole;
import cl.dssm.soporteimagenes.repository.PortalImageRequestLogRepository;
import cl.dssm.soporteimagenes.repository.PortalImageRequestRepository;
import cl.dssm.soporteimagenes.repository.PortalTopicRepository;
import cl.dssm.soporteimagenes.repository.SupportPortalRepository;
import cl.dssm.soporteimagenes.repository.UserRepository;
import cl.dssm.soporteimagenes.repository.UserPortalAssignmentRepository;
import cl.dssm.soporteimagenes.util.CsvUtils;
import cl.dssm.soporteimagenes.util.ExcelExportUtils;
import cl.dssm.soporteimagenes.util.PhoneUtils;
import cl.dssm.soporteimagenes.util.RutUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PortalImageRequestService {
    private static final Pattern PASSWORD_LIKE_RESPONSE = Pattern.compile("(?i).*(clave|contraseña|password)\\s*(nueva|temporal)?\\s*(es|:|=)\\s*\\S{4,}.*");

    private final PortalImageRequestRepository repository;
    private final PortalImageRequestLogRepository logRepository;
    private final UserRepository userRepository;
    private final UserPortalAssignmentRepository assignmentRepository;
    private final SupportPortalRepository supportPortalRepository;
    private final PortalTopicRepository portalTopicRepository;
    private final FolioService folioService;
    private final PortalImageRequestMapper mapper;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final CaptchaService captchaService;
    private final RequestAttachmentService attachmentService;

    @Transactional
    public PortalImageRequestResponseDto createPublicRequest(CreatePortalImageRequestDto dto) {
        return createPublicRequest(dto, List.of());
    }

    @Transactional
    public PortalImageRequestResponseDto createPublicRequest(CreatePortalImageRequestDto dto, List<MultipartFile> attachments) {
        SupportPortal supportPortal = resolveSupportPortal(dto.portalId(), dto.portalType());
        PortalTopic portalTopic = resolvePortalTopic(supportPortal, dto.topicId(), dto.difficultyType());
        DifficultyType difficultyType = legacyDifficultyType(portalTopic, dto.difficultyType());
        PortalType portalType = legacyPortalType(supportPortal, dto.portalType());

        validatePublicRequest(dto, difficultyType, portalTopic);
        String formattedRut = RutUtils.format(dto.rut());

        User assignedReferent = findReferent(supportPortal, portalType, portalTopic, difficultyType);

        PortalImageRequest request = PortalImageRequest.builder()
                .folio(folioService.nextFolio())
                .fullName(dto.fullName().trim())
                .rut(formattedRut)
                .portalType(portalType)
                .supportPortal(supportPortal)
                .email(blankToNull(dto.email()))
                .phone(PhoneUtils.normalizeMobile(dto.phone()))
                .fixedPhone(PhoneUtils.normalizeFixedPhone(dto.fixedPhone()))
                .difficultyType(difficultyType)
                .portalTopic(portalTopic)
                .otherDetail(blankToNull(dto.otherDetail()))
                .userObservation(blankToNull(dto.userObservation()))
                .consentAccepted(dto.consentAccepted())
                .source("QR_FORM")
                .status(RequestStatus.PENDIENTE)
                .assignedUser(assignedReferent)
                .build();

        PortalImageRequest saved = repository.save(request);
        if (attachments != null && !attachments.isEmpty()) {
            attachmentService.saveAttachments(saved, attachments);
        }
        logRepository.save(PortalImageRequestLog.builder()
                .request(saved)
                .action(LogAction.CREACION_SOLICITUD)
                .newStatus(saved.getStatus())
                .observation("Solicitud ingresada desde formulario público - " + supportPortal.getName())
                .build());
        if (assignedReferent != null) {
            logRepository.save(PortalImageRequestLog.builder()
                    .request(saved)
                    .user(assignedReferent)
                    .action(LogAction.ACTUALIZACION_SOLICITUD)
                    .newStatus(saved.getStatus())
                    .observation("Asignación automática a referente del portal: " + assignedReferent.getFullName())
                    .build());
        }

        NotificationService.NotificationResult notification = notificationService.sendAcknowledgement(saved);
        if (notification.sent()) {
            saved.setAcknowledgementSentAt(LocalDateTime.now());
            saved.setLastNotificationError(null);
            saved = repository.save(saved);
            logRepository.save(PortalImageRequestLog.builder()
                    .request(saved)
                    .action(LogAction.ENVIO_CORREO)
                    .newStatus(saved.getStatus())
                    .observation("Correo de recepción enviado al solicitante")
                    .build());
        } else if (!notification.skipped()) {
            saved.setLastNotificationError(notification.message());
            saved = repository.save(saved);
            logRepository.save(PortalImageRequestLog.builder()
                    .request(saved)
                    .action(LogAction.ERROR_ENVIO_CORREO)
                    .newStatus(saved.getStatus())
                    .observation("Error en correo de recepción: " + notification.message())
                    .build());
        }

        saved = notifyReferentsAboutNewRequest(saved, supportPortal, portalTopic, assignedReferent);

        return mapper.toDto(repository.save(saved));
    }

    @Transactional(readOnly = true)
    public PublicRequestStatusDto getPublicStatus(String folio, String rut) {
        if (!RutUtils.isValid(rut)) {
            throw new ResponseStatusException(BAD_REQUEST, "RUT inválido");
        }
        PortalImageRequest request = repository.findByFolio(folio)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Folio no encontrado"));
        String formattedRut = RutUtils.format(rut);
        if (!request.getRut().equalsIgnoreCase(formattedRut)) {
            throw new ResponseStatusException(NOT_FOUND, "No se encontró una solicitud asociada al folio y RUT ingresados");
        }
        return mapper.toPublicStatusDto(request);
    }

    @Transactional(readOnly = true)
    public Page<PortalImageRequestResponseDto> search(
            PortalType portalType,
            Long portalId,
            RequestStatus status,
            DifficultyType difficultyType,
            Long topicId,
            String rut,
            String folio,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        Optional<User> currentUser = currentUserService.getCurrentUser();
        Specification<PortalImageRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (portalId != null) predicates.add(cb.equal(root.get("supportPortal").get("id"), portalId));
            if (portalType != null) predicates.add(cb.equal(root.get("portalType"), portalType));
            if (topicId != null) predicates.add(cb.equal(root.get("portalTopic").get("id"), topicId));
            if (difficultyType != null) predicates.add(cb.equal(root.get("difficultyType"), difficultyType));
            if (rut != null && !rut.isBlank()) predicates.add(cb.like(cb.upper(root.get("rut")), "%" + rut.trim().toUpperCase() + "%"));
            if (folio != null && !folio.isBlank()) predicates.add(cb.like(cb.upper(root.get("folio")), "%" + folio.trim().toUpperCase() + "%"));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            if (to != null) predicates.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            currentUser.ifPresent(user -> {
                if (user.getRole() == UserRole.REFERENTE_DSSM) {
                    applyReferentScope(predicates, root, cb, user);
                }
            });
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PortalImageRequestResponseDto getById(Long id) {
        return mapper.toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<RequestLogDto> getLogs(Long id) {
        findEntity(id);
        return logRepository.findByRequest_IdOrderByCreatedAtDesc(id).stream().map(mapper::toLogDto).toList();
    }

    @Transactional
    public PortalImageRequestResponseDto updateStatus(Long id, UpdateRequestStatusDto dto) {
        validatePublicResponse(dto.publicResponse());
        PortalImageRequest request = findEntity(id);
        RequestStatus previous = request.getStatus();
        request.setStatus(dto.status());

        if (dto.status() == RequestStatus.RESUELTO || dto.status() == RequestStatus.NO_CORRESPONDE) {
            request.setResolvedAt(LocalDateTime.now());
        } else {
            request.setResolvedAt(null);
        }

        if (dto.assignedUserId() != null) {
            User assigned = userRepository.findById(dto.assignedUserId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Funcionario asignado no encontrado"));
            request.setAssignedUser(assigned);
        } else if (request.getAssignedUser() == null) {
            currentUserService.getCurrentUser().ifPresent(request::setAssignedUser);
        }

        if (dto.observation() != null && !dto.observation().isBlank()) {
            request.setInternalObservation(dto.observation().trim());
        }

        if (dto.publicResponse() != null && !dto.publicResponse().isBlank()) {
            request.setPublicResponse(dto.publicResponse().trim());
        }

        PortalImageRequest saved = repository.save(request);
        logRepository.save(PortalImageRequestLog.builder()
                .request(saved)
                .user(currentUserService.getCurrentUser().orElse(null))
                .action(LogAction.CAMBIO_ESTADO)
                .previousStatus(previous)
                .newStatus(dto.status())
                .observation(blankToNull(dto.observation()))
                .build());

        if (Boolean.TRUE.equals(dto.notifyRequester())) {
            saved = notifyRequester(saved);
        }

        return mapper.toDto(saved);
    }

    @Transactional
    public PortalImageRequestResponseDto updateInternalObservation(Long id, UpdateInternalObservationDto dto) {
        PortalImageRequest request = findEntity(id);
        request.setInternalObservation(dto.internalObservation().trim());
        PortalImageRequest saved = repository.save(request);
        logRepository.save(PortalImageRequestLog.builder()
                .request(saved)
                .user(currentUserService.getCurrentUser().orElse(null))
                .action(LogAction.OBSERVACION_INTERNA)
                .observation(dto.internalObservation().trim())
                .build());
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(PortalType portalType, Long portalId, RequestStatus status, DifficultyType difficultyType, Long topicId, LocalDate from, LocalDate to) {
        Page<PortalImageRequestResponseDto> page = search(portalType, portalId, status, difficultyType, topicId, null, null, from, to, Pageable.unpaged());
        StringBuilder sb = new StringBuilder();
        sb.append("Folio;Portal;Tematica;Fecha ingreso;Estado;Nombre;RUT;Correo;Celular;Telefono fijo;Tipo dificultad;Detalle otro;Observacion usuario;Observacion interna;Respuesta al solicitante;Correo recepcion enviado;Correo respuesta enviado;Error ultimo correo;Funcionario asignado;Fecha resolucion;Cantidad adjuntos\n");
        for (PortalImageRequestResponseDto r : page.getContent()) {
            sb.append(CsvUtils.escape(r.folio())).append(';')
                    .append(CsvUtils.escape(r.portalName())).append(';')
                    .append(CsvUtils.escape(r.topicName())).append(';')
                    .append(CsvUtils.escape(String.valueOf(r.createdAt()))).append(';')
                    .append(CsvUtils.escape(String.valueOf(r.status()))).append(';')
                    .append(CsvUtils.escape(r.fullName())).append(';')
                    .append(CsvUtils.escape(r.rut())).append(';')
                    .append(CsvUtils.escape(r.email())).append(';')
                    .append(CsvUtils.escape(r.phone())).append(';')
                    .append(CsvUtils.escape(r.fixedPhone())).append(';')
                    .append(CsvUtils.escape(String.valueOf(r.difficultyType()))).append(';')
                    .append(CsvUtils.escape(r.otherDetail())).append(';')
                    .append(CsvUtils.escape(r.userObservation())).append(';')
                    .append(CsvUtils.escape(r.internalObservation())).append(';')
                    .append(CsvUtils.escape(r.publicResponse())).append(';')
                    .append(CsvUtils.escape(String.valueOf(r.acknowledgementSentAt()))).append(';')
                    .append(CsvUtils.escape(String.valueOf(r.responseSentAt()))).append(';')
                    .append(CsvUtils.escape(r.lastNotificationError())).append(';')
                    .append(CsvUtils.escape(r.assignedUserName())).append(';')
                    .append(CsvUtils.escape(String.valueOf(r.resolvedAt()))).append(';')
                    .append(CsvUtils.escape(String.valueOf(r.attachmentCount()))).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel(PortalType portalType, Long portalId, RequestStatus status, DifficultyType difficultyType, Long topicId, LocalDate from, LocalDate to) {
        Page<PortalImageRequestResponseDto> page = search(portalType, portalId, status, difficultyType, topicId, null, null, from, to, Pageable.unpaged());
        return ExcelExportUtils.requestsToXlsx(page.getContent());
    }

    private PortalImageRequest notifyReferentsAboutNewRequest(PortalImageRequest request, SupportPortal supportPortal, PortalTopic portalTopic, User assignedReferent) {
        List<User> referents = findNotificationReferents(supportPortal, portalTopic, assignedReferent);
        if (referents.isEmpty()) {
            logRepository.save(PortalImageRequestLog.builder()
                    .request(request)
                    .action(LogAction.ACTUALIZACION_SOLICITUD)
                    .newStatus(request.getStatus())
                    .observation("No se encontró referente activo para notificar por correo")
                    .build());
            return request;
        }

        PortalImageRequest saved = request;
        for (User referent : referents) {
            NotificationService.NotificationResult result = notificationService.sendNewRequestToReferent(saved, referent);
            if (result.sent()) {
                saved.setLastNotificationError(null);
                saved = repository.save(saved);
                logRepository.save(PortalImageRequestLog.builder()
                        .request(saved)
                        .user(referent)
                        .action(LogAction.ENVIO_CORREO)
                        .newStatus(saved.getStatus())
                        .observation("Correo de nueva solicitud enviado al referente " + referent.getFullName())
                        .build());
            } else if (!result.skipped()) {
                String message = result.message() == null ? "No fue posible enviar correo al referente" : result.message();
                saved.setLastNotificationError(message);
                saved = repository.save(saved);
                logRepository.save(PortalImageRequestLog.builder()
                        .request(saved)
                        .user(referent)
                        .action(LogAction.ERROR_ENVIO_CORREO)
                        .newStatus(saved.getStatus())
                        .observation("Error al enviar correo al referente " + referent.getFullName() + ": " + message)
                        .build());
            }
        }
        return saved;
    }

    private List<User> findNotificationReferents(SupportPortal supportPortal, PortalTopic portalTopic, User assignedReferent) {
        Map<Long, User> referents = new LinkedHashMap<>();
        if (assignedReferent != null && assignedReferent.isActive() && assignedReferent.getEmail() != null && !assignedReferent.getEmail().isBlank()) {
            referents.put(assignedReferent.getId(), assignedReferent);
        }

        if (supportPortal != null) {
            List<UserPortalAssignment> assignments;
            if (portalTopic != null) {
                assignments = new ArrayList<>();
                assignments.addAll(assignmentRepository.findAllByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopic_Id(UserRole.REFERENTE_DSSM, supportPortal.getId(), portalTopic.getId()));
                assignments.addAll(assignmentRepository.findAllByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopicIsNull(UserRole.REFERENTE_DSSM, supportPortal.getId()));
            } else {
                assignments = assignmentRepository.findAllByUser_RoleAndUser_ActiveTrueAndSupportPortal_Id(UserRole.REFERENTE_DSSM, supportPortal.getId());
            }
            for (UserPortalAssignment assignment : assignments) {
                User user = assignment.getUser();
                if (user != null && user.isActive() && user.getEmail() != null && !user.getEmail().isBlank()) {
                    referents.put(user.getId(), user);
                }
            }
        }
        return new ArrayList<>(referents.values());
    }

    private PortalImageRequest notifyRequester(PortalImageRequest request) {
        NotificationService.NotificationResult notification = notificationService.sendResponse(request);
        User currentUser = currentUserService.getCurrentUser().orElse(null);
        if (notification.sent()) {
            request.setResponseSentAt(LocalDateTime.now());
            request.setLastNotificationError(null);
            PortalImageRequest saved = repository.save(request);
            logRepository.save(PortalImageRequestLog.builder()
                    .request(saved)
                    .user(currentUser)
                    .action(LogAction.ENVIO_CORREO)
                    .newStatus(saved.getStatus())
                    .observation("Correo de respuesta enviado al solicitante")
                    .build());
            return saved;
        }

        String message = notification.message() == null ? "No fue posible enviar el correo" : notification.message();
        request.setLastNotificationError(message);
        PortalImageRequest saved = repository.save(request);
        logRepository.save(PortalImageRequestLog.builder()
                .request(saved)
                .user(currentUser)
                .action(notification.skipped() ? LogAction.ACTUALIZACION_SOLICITUD : LogAction.ERROR_ENVIO_CORREO)
                .newStatus(saved.getStatus())
                .observation(notification.skipped() ? "Correo no enviado: " + message : "Error al enviar correo de respuesta: " + message)
                .build());
        return saved;
    }

    private User findReferent(SupportPortal supportPortal, PortalType portalType, PortalTopic portalTopic, DifficultyType difficultyType) {
        if (supportPortal != null) {
            Optional<User> topicReferent = portalTopic == null
                    ? Optional.empty()
                    : assignmentRepository.findFirstByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopic_Id(UserRole.REFERENTE_DSSM, supportPortal.getId(), portalTopic.getId())
                    .map(UserPortalAssignment::getUser);
            return topicReferent
                    .or(() -> assignmentRepository.findFirstByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopicIsNull(UserRole.REFERENTE_DSSM, supportPortal.getId()).map(UserPortalAssignment::getUser))
                    .or(() -> assignmentRepository.findFirstByUser_RoleAndUser_ActiveTrueAndSupportPortal_Id(UserRole.REFERENTE_DSSM, supportPortal.getId()).map(UserPortalAssignment::getUser))
                    .or(() -> userRepository.findFirstByRoleAndActiveTrueAndSupportPortal_IdAndPortalTopic_Id(UserRole.REFERENTE_DSSM, supportPortal.getId(), portalTopic == null ? -1L : portalTopic.getId()))
                    .or(() -> userRepository.findFirstByRoleAndActiveTrueAndSupportPortal_IdAndDifficultyType(UserRole.REFERENTE_DSSM, supportPortal.getId(), difficultyType))
                    .or(() -> userRepository.findFirstByRoleAndActiveTrueAndSupportPortal_IdAndPortalTopicIsNullAndDifficultyTypeIsNull(UserRole.REFERENTE_DSSM, supportPortal.getId()))
                    .or(() -> userRepository.findFirstByRoleAndActiveTrueAndSupportPortal_IdAndDifficultyTypeIsNull(UserRole.REFERENTE_DSSM, supportPortal.getId()))
                    .or(() -> userRepository.findFirstByRoleAndActiveTrueAndSupportPortal_Id(UserRole.REFERENTE_DSSM, supportPortal.getId()))
                    .orElse(null);
        }
        return userRepository.findFirstByRoleAndActiveTrueAndPortalTypeAndDifficultyType(UserRole.REFERENTE_DSSM, portalType, difficultyType)
                .or(() -> userRepository.findFirstByRoleAndActiveTrueAndPortalTypeAndDifficultyTypeIsNull(UserRole.REFERENTE_DSSM, portalType))
                .orElse(null);
    }

    private PortalImageRequest findEntity(Long id) {
        PortalImageRequest request = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Solicitud no encontrada"));
        currentUserService.getCurrentUser().ifPresent(user -> {
            if (user.getRole() == UserRole.REFERENTE_DSSM) {
                if (!isRequestWithinReferentScope(user, request)) {
                    throw new ResponseStatusException(FORBIDDEN, "No tiene permisos para gestionar solicitudes fuera de su portal o temática asignada");
                }
            }
        });
        return request;
    }


    private void applyReferentScope(List<Predicate> predicates, jakarta.persistence.criteria.Root<PortalImageRequest> root, jakarta.persistence.criteria.CriteriaBuilder cb, User user) {
        List<UserPortalAssignment> assignments = effectiveAssignments(user);
        if (!assignments.isEmpty()) {
            List<Predicate> allowed = new ArrayList<>();
            for (UserPortalAssignment assignment : assignments) {
                if (assignment.getSupportPortal() == null) continue;
                Predicate portalPredicate = cb.equal(root.get("supportPortal").get("id"), assignment.getSupportPortal().getId());
                if (assignment.getPortalTopic() != null) {
                    portalPredicate = cb.and(portalPredicate, cb.equal(root.get("portalTopic").get("id"), assignment.getPortalTopic().getId()));
                }
                allowed.add(portalPredicate);
            }
            if (!allowed.isEmpty()) {
                predicates.add(cb.or(allowed.toArray(new Predicate[0])));
                return;
            }
        }

        if (user.getSupportPortal() != null) {
            predicates.add(cb.equal(root.get("supportPortal").get("id"), user.getSupportPortal().getId()));
        } else if (user.getPortalType() != null) {
            predicates.add(cb.equal(root.get("portalType"), user.getPortalType()));
        }

        if (user.getPortalTopic() != null) {
            predicates.add(cb.equal(root.get("portalTopic").get("id"), user.getPortalTopic().getId()));
        } else if (user.getDifficultyType() != null) {
            predicates.add(cb.equal(root.get("difficultyType"), user.getDifficultyType()));
        }
    }

    private boolean isRequestWithinReferentScope(User user, PortalImageRequest request) {
        List<UserPortalAssignment> assignments = effectiveAssignments(user);
        if (!assignments.isEmpty()) {
            return assignments.stream().anyMatch(assignment -> {
                if (assignment.getSupportPortal() == null || request.getSupportPortal() == null) return false;
                boolean portalMatches = assignment.getSupportPortal().getId().equals(request.getSupportPortal().getId());
                if (!portalMatches) return false;
                if (assignment.getPortalTopic() != null) {
                    return request.getPortalTopic() != null && assignment.getPortalTopic().getId().equals(request.getPortalTopic().getId());
                }
                return true;
            });
        }

        boolean portalMatches = true;
        if (user.getSupportPortal() != null) {
            portalMatches = request.getSupportPortal() != null && user.getSupportPortal().getId().equals(request.getSupportPortal().getId());
        } else if (user.getPortalType() != null) {
            portalMatches = request.getPortalType() == user.getPortalType();
        }
        if (!portalMatches) return false;

        if (user.getPortalTopic() != null) {
            return request.getPortalTopic() != null && user.getPortalTopic().getId().equals(request.getPortalTopic().getId());
        }
        return user.getDifficultyType() == null || request.getDifficultyType() == user.getDifficultyType();
    }

    private List<UserPortalAssignment> effectiveAssignments(User user) {
        if (user.getPortalAssignments() != null && !user.getPortalAssignments().isEmpty()) {
            return user.getPortalAssignments();
        }
        if (user.getSupportPortal() != null) {
            return List.of(UserPortalAssignment.builder()
                    .user(user)
                    .supportPortal(user.getSupportPortal())
                    .portalTopic(user.getPortalTopic())
                    .build());
        }
        return List.of();
    }

    private void validatePublicRequest(CreatePortalImageRequestDto dto, DifficultyType difficultyType, PortalTopic topic) {
        if (dto.website() != null && !dto.website().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Solicitud rechazada por control anti-spam");
        }
        if (!captchaService.validate(dto.captchaId(), dto.captchaAnswer())) {
            throw new ResponseStatusException(BAD_REQUEST, "Validación anti-spam incorrecta o expirada. Actualice el desafío e intente nuevamente");
        }
        if (!RutUtils.isValid(dto.rut())) {
            throw new ResponseStatusException(BAD_REQUEST, "RUT inválido");
        }
        if (dto.email() != null && !dto.email().isBlank() && !dto.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(BAD_REQUEST, "Correo electrónico inválido");
        }
        if (!PhoneUtils.isValidMobile(dto.phone())) {
            throw new ResponseStatusException(BAD_REQUEST, "Celular inválido. Use formato chileno, por ejemplo: 9 1234 5678");
        }
        if (!PhoneUtils.isValidFixedPhone(dto.fixedPhone())) {
            throw new ResponseStatusException(BAD_REQUEST, "Teléfono fijo inválido");
        }
        if ((dto.phone() == null || dto.phone().isBlank()) && (dto.fixedPhone() == null || dto.fixedPhone().isBlank())) {
            throw new ResponseStatusException(BAD_REQUEST, "Debe ingresar al menos un teléfono de contacto: celular o teléfono fijo");
        }
        boolean requiresDetail = topic != null && topic.isRequiresDetail();
        boolean legacyOtherWithoutTopic = difficultyType == DifficultyType.OTRO && (topic == null || "OTRO".equals(topic.getCode()));
        if ((requiresDetail || legacyOtherWithoutTopic) && (dto.otherDetail() == null || dto.otherDetail().isBlank())) {
            throw new ResponseStatusException(BAD_REQUEST, "Debe especificar el detalle de la dificultad seleccionada");
        }
        if (difficultyType == DifficultyType.SIN_CORREO_REGISTRADO && (dto.phone() == null || dto.phone().isBlank()) && (dto.fixedPhone() == null || dto.fixedPhone().isBlank())) {
            throw new ResponseStatusException(BAD_REQUEST, "Debe ingresar teléfono de contacto si no cuenta con correo registrado");
        }
        if (difficultyType != DifficultyType.SIN_CORREO_REGISTRADO && (dto.email() == null || dto.email().isBlank())) {
            throw new ResponseStatusException(BAD_REQUEST, "Debe ingresar correo electrónico");
        }
    }

    private SupportPortal resolveSupportPortal(Long portalId, PortalType legacyPortalType) {
        if (portalId != null) {
            SupportPortal portal = supportPortalRepository.findById(portalId)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Portal no válido"));
            if (!portal.isActive()) {
                throw new ResponseStatusException(BAD_REQUEST, "El portal seleccionado no se encuentra activo");
            }
            return portal;
        }
        String code = legacyPortalType == null ? PortalType.PORTAL_IMAGENES.name() : legacyPortalType.name();
        return supportPortalRepository.findByCode(code)
                .orElseGet(() -> supportPortalRepository.findByCode("PORTAL_IMAGENES")
                        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No existe portal activo configurado")));
    }

    private PortalTopic resolvePortalTopic(SupportPortal portal, Long topicId, DifficultyType legacyDifficultyType) {
        if (topicId != null) {
            PortalTopic topic = portalTopicRepository.findById(topicId)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Temática no válida"));
            if (!topic.getPortal().getId().equals(portal.getId())) {
                throw new ResponseStatusException(BAD_REQUEST, "La temática seleccionada no pertenece al portal indicado");
            }
            if (!topic.isActive()) {
                throw new ResponseStatusException(BAD_REQUEST, "La temática seleccionada no se encuentra activa");
            }
            return topic;
        }
        DifficultyType fallback = legacyDifficultyType == null ? DifficultyType.CONTRASENA_NO_FUNCIONA : legacyDifficultyType;
        return portalTopicRepository.findByPortal_IdAndCode(portal.getId(), fallback.name()).orElse(null);
    }

    private PortalType legacyPortalType(SupportPortal portal, PortalType fallback) {
        if (portal != null) {
            try {
                return PortalType.valueOf(portal.getCode());
            } catch (Exception ignored) {
                return PortalType.PORTAL_IMAGENES;
            }
        }
        return fallback == null ? PortalType.PORTAL_IMAGENES : fallback;
    }

    private DifficultyType legacyDifficultyType(PortalTopic topic, DifficultyType fallback) {
        if (topic != null) {
            try {
                return DifficultyType.valueOf(topic.getCode());
            } catch (Exception ignored) {
                return DifficultyType.OTRO;
            }
        }
        return fallback == null ? DifficultyType.OTRO : fallback;
    }

    private void validatePublicResponse(String publicResponse) {
        if (publicResponse != null && PASSWORD_LIKE_RESPONSE.matcher(publicResponse).matches()) {
            throw new ResponseStatusException(BAD_REQUEST, "Por seguridad, la respuesta visible al solicitante no debe contener claves, contraseñas ni credenciales. Indique que el usuario debe recuperar o actualizar su contraseña desde el portal correspondiente.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
