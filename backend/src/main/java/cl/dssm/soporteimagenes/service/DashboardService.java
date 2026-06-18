package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.DashboardSummaryDto;
import cl.dssm.soporteimagenes.entity.PortalImageRequest;
import cl.dssm.soporteimagenes.entity.User;
import cl.dssm.soporteimagenes.entity.UserPortalAssignment;
import cl.dssm.soporteimagenes.enums.RequestStatus;
import cl.dssm.soporteimagenes.enums.UserRole;
import cl.dssm.soporteimagenes.repository.PortalImageRequestRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final PortalImageRequestRepository repository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public DashboardSummaryDto summary(Long portalId) {
        Optional<User> currentUser = currentUserService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        Specification<PortalImageRequest> baseSpec = buildBaseSpec(portalId, currentUser);
        List<PortalImageRequest> requests = repository.findAll(baseSpec);

        Map<String, Long> byTopic = requests.stream()
                .collect(Collectors.groupingBy(this::topicLabel, LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> byPortal = requests.stream()
                .collect(Collectors.groupingBy(this::portalLabel, LinkedHashMap::new, Collectors.counting()));

        return new DashboardSummaryDto(
                requests.size(),
                countStatus(requests, RequestStatus.PENDIENTE),
                countStatus(requests, RequestStatus.EN_REVISION),
                countStatus(requests, RequestStatus.CONTACTADO),
                countStatus(requests, RequestStatus.RESUELTO),
                countStatus(requests, RequestStatus.NO_CORRESPONDE),
                byTopic,
                byPortal,
                requests.stream().filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(now.minusDays(7))).count(),
                requests.stream().filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(now.minusDays(30))).count()
        );
    }

    private Specification<PortalImageRequest> buildBaseSpec(Long portalId, Optional<User> currentUser) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (portalId != null) {
                predicates.add(cb.equal(root.get("supportPortal").get("id"), portalId));
            }
            currentUser.ifPresent(user -> {
                if (user.getRole() == UserRole.REFERENTE_DSSM) {
                    applyReferentScope(predicates, root, cb, user);
                }
            });
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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

    private List<UserPortalAssignment> effectiveAssignments(User user) {
        if (user.getPortalAssignments() != null && !user.getPortalAssignments().isEmpty()) {
            return user.getPortalAssignments();
        }
        if (user.getSupportPortal() != null) {
            UserPortalAssignment assignment = UserPortalAssignment.builder()
                    .user(user)
                    .supportPortal(user.getSupportPortal())
                    .portalTopic(user.getPortalTopic())
                    .build();
            return List.of(assignment);
        }
        return List.of();
    }

    private long countStatus(List<PortalImageRequest> requests, RequestStatus status) {
        return requests.stream().filter(r -> r.getStatus() == status).count();
    }

    private String portalLabel(PortalImageRequest request) {
        if (request.getSupportPortal() != null && request.getSupportPortal().getName() != null) {
            return request.getSupportPortal().getName();
        }
        return request.getPortalType() == null ? "Sin portal" : request.getPortalType().getDisplayName();
    }

    private String topicLabel(PortalImageRequest request) {
        if (request.getPortalTopic() != null && request.getPortalTopic().getName() != null) {
            return request.getPortalTopic().getName();
        }
        if (request.getDifficultyType() == null) {
            return "Sin temática";
        }
        return switch (request.getDifficultyType()) {
            case CONTRASENA_NO_FUNCIONA -> "Mi contraseña no funciona";
            case DATOS_CONTACTO_NO_ACTUALIZADOS -> "Mis datos de contacto no están actualizados";
            case SIN_CORREO_REGISTRADO -> "No he registrado un correo electrónico";
            case OTRO -> "Otro";
        };
    }
}
