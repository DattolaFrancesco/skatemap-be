package fra.skatemap.repositories;

import fra.skatemap.entities.Media;
import fra.skatemap.entities.Spot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
    long countBySpot(Spot spot);
    @Query("SELECT m FROM Media m WHERE m.spot.id = :spotId AND TYPE(m) = :type")
    Page<Media> findBySpotAndType(UUID spotId, Class<? extends Media> type, Pageable pageable);
    Page<Media> findBySpotId(UUID spotId, Pageable pageable);
}
