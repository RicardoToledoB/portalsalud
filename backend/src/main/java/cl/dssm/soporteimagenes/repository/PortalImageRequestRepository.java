package cl.dssm.soporteimagenes.repository;

import cl.dssm.soporteimagenes.entity.PortalImageRequest;
import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.RequestStatus;
import cl.dssm.soporteimagenes.enums.PortalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PortalImageRequestRepository extends JpaRepository<PortalImageRequest, Long>, JpaSpecificationExecutor<PortalImageRequest> {
    Optional<PortalImageRequest> findByFolio(String folio);
    boolean existsByFolio(String folio);
    long countByStatus(RequestStatus status);
    long countByDifficultyType(DifficultyType difficultyType);
    long countByPortalType(PortalType portalType);
    long countBySupportPortal_Id(Long portalId);
    long countByPortalTopic_Id(Long topicId);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    Page<PortalImageRequest> findByRutContainingIgnoreCase(String rut, Pageable pageable);
}
