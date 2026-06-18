package cl.dssm.soporteimagenes.repository;

import cl.dssm.soporteimagenes.entity.PortalImageRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortalImageRequestLogRepository extends JpaRepository<PortalImageRequestLog, Long> {
    List<PortalImageRequestLog> findByRequest_IdOrderByCreatedAtDesc(Long requestId);
}
