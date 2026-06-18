package cl.dssm.soporteimagenes.repository;

import cl.dssm.soporteimagenes.entity.PortalTopic;
import cl.dssm.soporteimagenes.entity.SupportPortal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortalTopicRepository extends JpaRepository<PortalTopic, Long> {
    boolean existsByPortalAndCode(SupportPortal portal, String code);
    List<PortalTopic> findByPortal_IdAndActiveTrueOrderByDisplayOrderAscNameAsc(Long portalId);
    List<PortalTopic> findByPortal_IdOrderByDisplayOrderAscNameAsc(Long portalId);
    Optional<PortalTopic> findByPortal_IdAndCode(Long portalId, String code);
}
