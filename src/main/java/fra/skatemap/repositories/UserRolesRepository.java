package fra.skatemap.repositories;

import fra.skatemap.entities.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRolesRepository extends JpaRepository<UserRole, UUID> {
    void deleteByUserId(UUID userId);
    UserRole findByUserId(UUID userId);
    Page<UserRole> findByRoleRoleNameIn(List<String> roleName, Pageable pageable);
}