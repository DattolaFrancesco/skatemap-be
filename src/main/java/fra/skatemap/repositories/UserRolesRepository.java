package fra.skatemap.repositories;

import fra.skatemap.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface UserRolesRepository extends JpaRepository<UserRole, UUID> {
    void deleteByUserId(UUID userId);
    UserRole findByUserId(UUID userId);
}