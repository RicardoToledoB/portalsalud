package cl.dssm.soporteimagenes.repository;

import cl.dssm.soporteimagenes.entity.UserPortalAssignment;
import cl.dssm.soporteimagenes.enums.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPortalAssignmentRepository extends JpaRepository<UserPortalAssignment, Long> {
    @EntityGraph(attributePaths = {"supportPortal", "portalTopic", "user"})
    List<UserPortalAssignment> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    Optional<UserPortalAssignment> findFirstByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopic_Id(
            UserRole role, Long portalId, Long topicId);

    Optional<UserPortalAssignment> findFirstByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopicIsNull(
            UserRole role, Long portalId);

    Optional<UserPortalAssignment> findFirstByUser_RoleAndUser_ActiveTrueAndSupportPortal_Id(
            UserRole role, Long portalId);
    @EntityGraph(attributePaths = {"supportPortal", "portalTopic", "user"})
    List<UserPortalAssignment> findAllByUser_RoleAndUser_ActiveTrueAndSupportPortal_Id(UserRole role, Long portalId);

    @EntityGraph(attributePaths = {"supportPortal", "portalTopic", "user"})
    List<UserPortalAssignment> findAllByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopic_Id(UserRole role, Long portalId, Long topicId);

    @EntityGraph(attributePaths = {"supportPortal", "portalTopic", "user"})
    List<UserPortalAssignment> findAllByUser_RoleAndUser_ActiveTrueAndSupportPortal_IdAndPortalTopicIsNull(UserRole role, Long portalId);

}
