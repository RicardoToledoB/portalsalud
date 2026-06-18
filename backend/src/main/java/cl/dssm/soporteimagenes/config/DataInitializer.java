package cl.dssm.soporteimagenes.config;

import cl.dssm.soporteimagenes.entity.User;
import cl.dssm.soporteimagenes.enums.UserRole;
import cl.dssm.soporteimagenes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap-admin.full-name:Administrador Portales Salud}")
    private String fullName;

    @Value("${app.bootstrap-admin.email:admin@dssm.cl}")
    private String email;

    @Value("${app.bootstrap-admin.password:Cambiar.2026!}")
    private String password;

    @Override
    public void run(String... args) {
        if (!enabled) return;
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) return;
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Debe definir una contraseña para el administrador inicial o deshabilitar bootstrap-admin.");
        }
        User admin = User.builder()
                .fullName(fullName)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
    }
}
