package cl.dssm.soporteimagenes.entity;

import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.RequestStatus;
import cl.dssm.soporteimagenes.enums.PortalType;
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
@Table(name = "portal_image_requests")
public class PortalImageRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @Enumerated(EnumType.STRING)
    @Column(name = "portal_type", nullable = false, length = 80)
    private PortalType portalType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_portal_id")
    private SupportPortal supportPortal;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String rut;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(name = "fixed_phone", length = 50)
    private String fixedPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_type", nullable = false, length = 80)
    private DifficultyType difficultyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_topic_id")
    private PortalTopic portalTopic;

    @Column(name = "other_detail", length = 500)
    private String otherDetail;

    @Column(name = "user_observation", length = 1000)
    private String userObservation;

    @Column(name = "consent_accepted", nullable = false)
    private boolean consentAccepted;

    @Column(nullable = false, length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestStatus status;

    @Column(name = "internal_observation", length = 1500)
    private String internalObservation;

    @Column(name = "public_response", length = 1500)
    private String publicResponse;

    @Column(name = "acknowledgement_sent_at")
    private LocalDateTime acknowledgementSentAt;

    @Column(name = "response_sent_at")
    private LocalDateTime responseSentAt;

    @Column(name = "last_notification_error", length = 1000)
    private String lastNotificationError;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Builder.Default
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestAttachment> attachments = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = this.status == null ? RequestStatus.PENDIENTE : this.status;
        this.portalType = this.portalType == null ? PortalType.PORTAL_IMAGENES : this.portalType;
        this.source = this.source == null ? "QR_FORM" : this.source;
        this.email = normalizeEmail(this.email);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.email = normalizeEmail(this.email);
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
