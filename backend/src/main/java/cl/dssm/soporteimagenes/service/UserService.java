package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.CreateUserDto;
import cl.dssm.soporteimagenes.dto.ResetPasswordDto;
import cl.dssm.soporteimagenes.dto.UpdateUserDto;
import cl.dssm.soporteimagenes.dto.UserDto;
import cl.dssm.soporteimagenes.dto.UserPortalAssignmentDto;
import cl.dssm.soporteimagenes.dto.UserPortalAssignmentInputDto;
import cl.dssm.soporteimagenes.entity.SupportPortal;
import cl.dssm.soporteimagenes.entity.PortalTopic;
import cl.dssm.soporteimagenes.entity.User;
import cl.dssm.soporteimagenes.entity.UserPortalAssignment;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.enums.UserRole;
import cl.dssm.soporteimagenes.repository.SupportPortalRepository;
import cl.dssm.soporteimagenes.repository.PortalTopicRepository;
import cl.dssm.soporteimagenes.repository.UserPortalAssignmentRepository;
import cl.dssm.soporteimagenes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SupportPortalRepository supportPortalRepository;
    private final PortalTopicRepository portalTopicRepository;
    private final UserPortalAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> listAssignableReferents() {
        User currentUser = currentUserService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Usuario no autenticado"));

        List<User> activeReferents = userRepository.findAll().stream()
                .filter(user -> user.isActive() && user.getRole() == UserRole.REFERENTE_DSSM)
                .toList();

        if (currentUser.getRole() == UserRole.ADMIN) {
            return activeReferents.stream().map(this::toDto).toList();
        }

        if (currentUser.getRole() != UserRole.REFERENTE_DSSM) {
            return List.of();
        }

        Set<Long> allowedPortalIds = assignedPortalIds(currentUser);
        if (allowedPortalIds.isEmpty()) {
            return List.of();
        }

        return activeReferents.stream()
                .filter(user -> !assignedPortalIds(user).isEmpty())
                .filter(user -> assignedPortalIds(user).stream().anyMatch(allowedPortalIds::contains))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserDto create(CreateUserDto dto) {
        String email = dto.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(BAD_REQUEST, "Ya existe un usuario con ese correo");
        }

        User user = User.builder()
                .fullName(dto.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(dto.password()))
                .role(dto.role())
                .portalType(dto.portalType())
                .difficultyType(dto.difficultyType())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        applyAssignments(saved, dto.role(), dto.portalId(), dto.topicId(), dto.portalAssignments(), true);
        return toDto(userRepository.save(saved));
    }

    @Transactional
    public UserDto update(Long id, UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        if (dto.fullName() != null && !dto.fullName().isBlank()) user.setFullName(dto.fullName().trim());
        if (dto.email() != null && !dto.email().isBlank()) {
            String email = dto.email().trim().toLowerCase();
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                throw new ResponseStatusException(BAD_REQUEST, "Ya existe otro usuario con ese correo");
            }
            user.setEmail(email);
        }
        if (dto.password() != null && !dto.password().isBlank()) user.setPasswordHash(passwordEncoder.encode(dto.password()));
        if (dto.role() != null) user.setRole(dto.role());
        if (dto.active() != null) user.setActive(dto.active());
        if (dto.portalType() != null) user.setPortalType(dto.portalType());
        user.setDifficultyType(dto.difficultyType());

        boolean assignmentsProvided = dto.portalAssignments() != null || dto.portalId() != null || dto.topicId() != null || dto.role() != null;
        if (assignmentsProvided) {
            applyAssignments(user, user.getRole(), dto.portalId(), dto.topicId(), dto.portalAssignments(), false);
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto resetPassword(Long id, ResetPasswordDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        user.setActive(active);
        return toDto(userRepository.save(user));
    }

    private void applyAssignments(User user, UserRole role, Long legacyPortalId, Long legacyTopicId, List<UserPortalAssignmentInputDto> assignments, boolean creating) {
        assignmentRepository.deleteByUser_Id(user.getId());
        user.getPortalAssignments().clear();

        if (role != UserRole.REFERENTE_DSSM) {
            user.setSupportPortal(null);
            user.setPortalTopic(null);
            user.setPortalType(null);
            user.setDifficultyType(null);
            return;
        }

        List<UserPortalAssignmentInputDto> inputs = normalizeAssignments(legacyPortalId, legacyTopicId, assignments);
        if (inputs.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Debe asignar al menos un portal al referente DSSM");
        }

        List<UserPortalAssignment> resolved = new ArrayList<>();
        for (UserPortalAssignmentInputDto input : inputs) {
            if (input == null || input.portalId() == null || input.portalId() == 0) continue;
            SupportPortal portal = resolvePortalIfPresent(input.portalId());
            PortalTopic topic = resolveTopicIfPresent(input.topicId(), portal);
            boolean duplicated = resolved.stream().anyMatch(a ->
                    a.getSupportPortal().getId().equals(portal.getId()) &&
                            ((a.getPortalTopic() == null && topic == null) ||
                                    (a.getPortalTopic() != null && topic != null && a.getPortalTopic().getId().equals(topic.getId()))));
            if (!duplicated) {
                resolved.add(UserPortalAssignment.builder()
                        .user(user)
                        .supportPortal(portal)
                        .portalTopic(topic)
                        .build());
            }
        }

        if (resolved.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Debe asignar al menos un portal válido al referente DSSM");
        }

        assignmentRepository.saveAll(resolved);
        user.getPortalAssignments().addAll(resolved);
        UserPortalAssignment primary = resolved.get(0);
        user.setSupportPortal(primary.getSupportPortal());
        user.setPortalTopic(primary.getPortalTopic());
        user.setPortalType(legacyPortal(null, primary.getSupportPortal()));
    }

    private List<UserPortalAssignmentInputDto> normalizeAssignments(Long legacyPortalId, Long legacyTopicId, List<UserPortalAssignmentInputDto> assignments) {
        if (assignments != null && !assignments.isEmpty()) {
            Map<String, UserPortalAssignmentInputDto> unique = new LinkedHashMap<>();
            for (UserPortalAssignmentInputDto a : assignments) {
                if (a == null || a.portalId() == null || a.portalId() == 0) continue;
                Long topicId = a.topicId() == null || a.topicId() == 0 ? null : a.topicId();
                unique.put(a.portalId() + ":" + (topicId == null ? "ALL" : topicId), new UserPortalAssignmentInputDto(a.portalId(), topicId));
            }
            return new ArrayList<>(unique.values());
        }
        if (legacyPortalId != null && legacyPortalId != 0) {
            Long topicId = legacyTopicId == null || legacyTopicId == 0 ? null : legacyTopicId;
            return List.of(new UserPortalAssignmentInputDto(legacyPortalId, topicId));
        }
        return List.of();
    }

    private SupportPortal resolvePortalIfPresent(Long portalId) {
        if (portalId == null || portalId == 0) return null;
        return supportPortalRepository.findById(portalId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Portal no encontrado"));
    }

    private PortalTopic resolveTopicIfPresent(Long topicId, SupportPortal supportPortal) {
        if (topicId == null || topicId == 0) return null;
        PortalTopic topic = portalTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Temática no encontrada"));
        if (supportPortal == null || topic.getPortal() == null || !topic.getPortal().getId().equals(supportPortal.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "La temática seleccionada no pertenece al portal asignado");
        }
        return topic;
    }

    private PortalType legacyPortal(PortalType portalType, SupportPortal supportPortal) {
        if (portalType != null) return portalType;
        if (supportPortal != null) {
            try {
                return PortalType.valueOf(supportPortal.getCode());
            } catch (Exception ignored) {
                return PortalType.PORTAL_IMAGENES;
            }
        }
        return PortalType.PORTAL_IMAGENES;
    }

    private Set<Long> assignedPortalIds(User user) {
        Set<Long> portalIds = new HashSet<>();
        if (user.getPortalAssignments() != null && !user.getPortalAssignments().isEmpty()) {
            user.getPortalAssignments().stream()
                    .filter(assignment -> assignment.getSupportPortal() != null)
                    .map(assignment -> assignment.getSupportPortal().getId())
                    .forEach(portalIds::add);
        }
        if (user.getSupportPortal() != null) {
            portalIds.add(user.getSupportPortal().getId());
        }
        return portalIds;
    }

    private UserDto toDto(User user) {
        List<UserPortalAssignmentDto> assignments = assignmentDtos(user);
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getPortalType(),
                user.getSupportPortal() == null ? null : user.getSupportPortal().getId(),
                portalDisplayName(user, assignments),
                user.getPortalTopic() == null ? null : user.getPortalTopic().getId(),
                user.getPortalTopic() == null ? null : user.getPortalTopic().getName(),
                assignments,
                user.getDifficultyType(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String portalDisplayName(User user, List<UserPortalAssignmentDto> assignments) {
        if (user.getRole() == UserRole.ADMIN) return "Todos los portales";
        if (assignments.size() > 1) return assignments.size() + " portales asignados";
        if (assignments.size() == 1) return assignments.get(0).portalName();
        if (user.getSupportPortal() != null) return user.getSupportPortal().getName();
        return user.getPortalType() == null ? null : user.getPortalType().getDisplayName();
    }

    private List<UserPortalAssignmentDto> assignmentDtos(User user) {
        List<UserPortalAssignment> assignments = user.getPortalAssignments() == null ? List.of() : user.getPortalAssignments();
        if (assignments.isEmpty() && user.getSupportPortal() != null) {
            return List.of(new UserPortalAssignmentDto(
                    user.getSupportPortal().getId(),
                    user.getSupportPortal().getName(),
                    user.getPortalTopic() == null ? null : user.getPortalTopic().getId(),
                    user.getPortalTopic() == null ? null : user.getPortalTopic().getName()
            ));
        }
        return assignments.stream()
                .filter(a -> a.getSupportPortal() != null)
                .sorted((a, b) -> {
                    int order = Integer.compare(a.getSupportPortal().getDisplayOrder(), b.getSupportPortal().getDisplayOrder());
                    if (order != 0) return order;
                    return a.getSupportPortal().getName().compareToIgnoreCase(b.getSupportPortal().getName());
                })
                .map(a -> new UserPortalAssignmentDto(
                        a.getSupportPortal().getId(),
                        a.getSupportPortal().getName(),
                        a.getPortalTopic() == null ? null : a.getPortalTopic().getId(),
                        a.getPortalTopic() == null ? null : a.getPortalTopic().getName()
                ))
                .toList();
    }
}
