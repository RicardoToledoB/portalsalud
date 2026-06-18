package cl.dssm.soporteimagenes.entity;

import cl.dssm.soporteimagenes.enums.UserRole;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.enums.DifficultyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "portal_type", length = 80)
    private PortalType portalType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_portal_id")
    private SupportPortal supportPortal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_topic_id")
    private PortalTopic portalTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_type", length = 80)
    private DifficultyType difficultyType;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<UserPortalAssignment> portalAssignments = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.email = this.email == null ? null : this.email.trim().toLowerCase();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.email = this.email == null ? null : this.email.trim().toLowerCase();
    }
}
