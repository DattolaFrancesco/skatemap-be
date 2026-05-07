package fra.skatemap.repositories;

import fra.skatemap.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolesRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleName(String roleName);
    Optional<Role> findById(UUID id);
    boolean existsByRoleName(String roleName);
}