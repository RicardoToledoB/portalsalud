package cl.dssm.soporteimagenes.repository;

import cl.dssm.soporteimagenes.entity.RequestAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestAttachmentRepository extends JpaRepository<RequestAttachment, Long> {
    List<RequestAttachment> findByRequest_IdOrderByCreatedAtAsc(Long requestId);
    Optional<RequestAttachment> findByIdAndRequest_Id(Long id, Long requestId);
}
