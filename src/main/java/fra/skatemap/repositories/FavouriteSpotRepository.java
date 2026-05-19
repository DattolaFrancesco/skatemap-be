package fra.skatemap.repositories;

import fra.skatemap.entities.FavouriteSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FavouriteSpotRepository extends JpaRepository<FavouriteSpot, UUID> {
    FavouriteSpot findBySpotIdAndUserId(UUID spotId, UUID userId);
    Page<FavouriteSpot> findByUserId(UUID userId, Pageable pageable);
}
