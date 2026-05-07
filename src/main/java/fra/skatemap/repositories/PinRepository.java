package fra.skatemap.repositories;

import fra.skatemap.entities.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PinRepository extends JpaRepository<Pin, UUID> {
    boolean existsByLatitudeAndLongitude(double latitude, double longitude);
}
