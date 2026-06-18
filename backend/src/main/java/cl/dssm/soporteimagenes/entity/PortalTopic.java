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
@Table(name = "portal_topics", uniqueConstraints = @UniqueConstraint(name = "uk_portal_topics_portal_code", columnNames = {"portal_id", "code"}))
public class PortalTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portal_id", nullable = false)
    private SupportPortal portal;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "requires_detail", nullable = false)
    private boolean requiresDetail;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.displayOrder = this.displayOrder == null ? 0 : this.displayOrder;
        this.code = normalizeCode(this.code);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.code = normalizeCode(this.code);
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase().replaceAll("[^A-Z0-9_]+", "_");
    }
}
