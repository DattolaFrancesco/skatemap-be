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
    Page<Media> findBySpotId(UUID spotId, Pageable pageable);
    Page<Media> findBySpotIdAndFormat(UUID id, String format,Pageable pageable);
}
