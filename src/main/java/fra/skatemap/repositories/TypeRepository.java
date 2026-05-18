package fra.skatemap.repositories;

import fra.skatemap.entities.Role;
import fra.skatemap.entities.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeRepository extends JpaRepository<Type, UUID> {
    Optional<Type> findBySpotType(String spotType);
    boolean existsBySpotType(String spotType);
}
