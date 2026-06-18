package cl.dssm.soporteimagenes.repository;

import cl.dssm.soporteimagenes.entity.User;
import cl.dssm.soporteimagenes.enums.DifficultyType;
import cl.dssm.soporteimagenes.enums.PortalType;
import cl.dssm.soporteimagenes.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"supportPortal", "portalTopic", "portalAssignments", "portalAssignments.supportPortal", "portalAssignments.portalTopic"})
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"supportPortal", "portalTopic", "portalAssignments", "portalAssignments.supportPortal", "portalAssignments.portalTopic"})
    Optional<User> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"supportPortal", "portalTopic", "portalAssignments", "portalAssignments.supportPortal", "portalAssignments.portalTopic"})
    List<User> findAll();

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<User> findFirstByRoleAndActiveTrueAndPortalTypeAndDifficultyType(UserRole role, PortalType portalType, DifficultyType difficultyType);
    Optional<User> findFirstByRoleAndActiveTrueAndPortalTypeAndDifficultyTypeIsNull(UserRole role, PortalType portalType);
    Optional<User> findFirstByRoleAndActiveTrueAndSupportPortal_Id(UserRole role, Long portalId);
    Optional<User> findFirstByRoleAndActiveTrueAndSupportPortal_IdAndPortalTopicIsNullAndDifficultyTypeIsNull(UserRole role, Long portalId);
    Optional<User> findFirstByRoleAndActiveTrueAndSupportPortal_IdAndPortalTopic_Id(UserRole role, Long portalId, Long topicId);
    Optional<User> findFirstByRoleAndActiveTrueAndSupportPortal_IdAndDifficultyTypeIsNull(UserRole role, Long portalId);
    Optional<User> findFirstByRoleAndActiveTrueAndSupportPortal_IdAndDifficultyType(UserRole role, Long portalId, DifficultyType difficultyType);
}
