package fra.skatemap.repositories;

import fra.skatemap.entities.FavouriteSpot;
import fra.skatemap.payloads.SpotsQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavouriteSpotRepository extends JpaRepository<FavouriteSpot, UUID> {
    FavouriteSpot findBySpotIdAndUserId(UUID spotId, UUID userId);
    Page<FavouriteSpot> findByUserId(UUID userId, Pageable pageable);
    long countByUserId(UUID id);
    @Query("""
    SELECT new fra.skatemap.payloads.SpotsQueryDTO(
        f.spot.id,
        f.spot.name,
        f.spot.latitude,
        f.spot.longitude,
        f.spot.city,
        CAST(f.spot.continents AS string),
        f.spot.risk,
        f.spot.country,
        f.spot.street,
        (SELECT m.link FROM Media m WHERE m.spot = f.spot AND TYPE(m) = Image ORDER BY m.id ASC LIMIT 1),
        f.spot.status
    )
    FROM FavouriteSpot f
    WHERE f.user.id = :userId
""")
    List<SpotsQueryDTO> findAllFavForList(@Param("userId") UUID userId);
}
