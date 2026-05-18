package fra.skatemap.repositories;

import fra.skatemap.entities.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpotRepository  extends JpaRepository<Spot, UUID> {
    boolean existsByName(String name);
    boolean existsByLatitudeAndLongitude(double latitude, double longitude);
}
