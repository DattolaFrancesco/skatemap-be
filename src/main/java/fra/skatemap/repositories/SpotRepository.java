package fra.skatemap.repositories;

import fra.skatemap.entities.Spot;
import fra.skatemap.enums.Status_spot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpotRepository  extends JpaRepository<Spot, UUID> {
    boolean existsByName(String name);
    boolean existsByLatitudeAndLongitude(double latitude, double longitude);
    Page<Spot> findByStatus(Status_spot status, Pageable pageable);
}
