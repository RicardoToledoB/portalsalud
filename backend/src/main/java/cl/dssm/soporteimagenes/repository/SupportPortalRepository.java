package cl.dssm.soporteimagenes.repository;

import cl.dssm.soporteimagenes.entity.SupportPortal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportPortalRepository extends JpaRepository<SupportPortal, Long> {
    Optional<SupportPortal> findByCode(String code);
    boolean existsByCode(String code);
    List<SupportPortal> findByActiveTrueOrderByDisplayOrderAscNameAsc();
    List<SupportPortal> findAllByOrderByDisplayOrderAscNameAsc();
}
