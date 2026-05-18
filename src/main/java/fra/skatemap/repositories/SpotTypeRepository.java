package fra.skatemap.repositories;

import fra.skatemap.entities.Spot;
import fra.skatemap.entities.SpotType;
import fra.skatemap.entities.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpotTypeRepository extends JpaRepository<SpotType, UUID> {
    boolean existsBySpotIdAndTypeId(UUID spotId, UUID typeId);
}
