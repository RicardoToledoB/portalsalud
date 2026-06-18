package cl.dssm.soporteimagenes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_portal_assignments",
        indexes = {
                @Index(name = "idx_user_portal_assignments_user", columnList = "user_id"),
                @Index(name = "idx_user_portal_assignments_portal", columnList = "support_portal_id"),
                @Index(name = "idx_user_portal_assignments_topic", columnList = "portal_topic_id")
        })
public class UserPortalAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "support_portal_id", nullable = false)
    private SupportPortal supportPortal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_topic_id")
    private PortalTopic portalTopic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
