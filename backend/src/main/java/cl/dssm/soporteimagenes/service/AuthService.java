package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.AuthRequest;
import cl.dssm.soporteimagenes.dto.AuthResponse;
import cl.dssm.soporteimagenes.entity.User;
import cl.dssm.soporteimagenes.dto.UserPortalAssignmentDto;
import cl.dssm.soporteimagenes.repository.UserRepository;
import cl.dssm.soporteimagenes.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas"));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getSupportPortal() == null ? null : user.getSupportPortal().getId(),
                user.getSupportPortal() == null ? null : user.getSupportPortal().getName(),
                user.getPortalTopic() == null ? null : user.getPortalTopic().getId(),
                user.getPortalTopic() == null ? null : user.getPortalTopic().getName(),
                assignmentDtos(user),
                user.getDifficultyType()
        );
    }

    private List<UserPortalAssignmentDto> assignmentDtos(User user) {
        if (user.getPortalAssignments() == null || user.getPortalAssignments().isEmpty()) {
            if (user.getSupportPortal() == null) return List.of();
            return List.of(new UserPortalAssignmentDto(
                    user.getSupportPortal().getId(),
                    user.getSupportPortal().getName(),
                    user.getPortalTopic() == null ? null : user.getPortalTopic().getId(),
                    user.getPortalTopic() == null ? null : user.getPortalTopic().getName()
            ));
        }
        return user.getPortalAssignments().stream()
                .filter(a -> a.getSupportPortal() != null)
                .map(a -> new UserPortalAssignmentDto(
                        a.getSupportPortal().getId(),
                        a.getSupportPortal().getName(),
                        a.getPortalTopic() == null ? null : a.getPortalTopic().getId(),
                        a.getPortalTopic() == null ? null : a.getPortalTopic().getName()
                ))
                .toList();
    }
}
