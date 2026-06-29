package fra.skatemap.repositories;

import fra.skatemap.entities.Spot;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.payloads.SpotListResponseDTO;
import fra.skatemap.payloads.SpotsQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpotRepository extends JpaRepository<Spot, UUID>, JpaSpecificationExecutor<Spot> {

    boolean existsByName(String name);
    boolean existsByLatitudeAndLongitude(double latitude, double longitude);
    Page<Spot> findByStatus(Status_spot status, Pageable pageable);
    Page<Spot> findByStatusAndUserId(Status_spot status, UUID id, Pageable pageable);
    Page<Spot> findByUserId(UUID id, Pageable pageable);
    @EntityGraph(attributePaths = {"media"})
    @Query(
            value = "SELECT s FROM Spot s",
            countQuery = "SELECT COUNT(s) FROM Spot s"
    )
    Page<Spot> findAll(Specification<Spot> spec, Pageable pageable);
    List<Spot> findByUserId(UUID id);
    long countByUserId(UUID id);
    boolean existsByStatus(Status_spot status);
    @Query("""
    SELECT 
        new fra.skatemap.payloads.SpotsQueryDTO( 
            s.id,s.name,s.latitude,s.longitude,s.city,CAST(s.continents AS string),s.risk,s.country,s.street,
            (SELECT m.link FROM Media m WHERE m.spot = s AND TYPE(m) = Image ORDER BY m.id ASC LIMIT 1),s.status)
            FROM Spot s WHERE s.status = :status ORDER BY s.id ASC
    """)
    List<SpotsQueryDTO> findAllForList(@Param("status")Status_spot status);
    @Query("""
    SELECT s.id, st.type.spotType
    FROM Spot s
    JOIN s.spotTypes st
    WHERE s.status = :status
    """)
    List<Object[]> findAllSpotTypes(@Param("status") Status_spot status);
    @Query("""
    SELECT 
        new fra.skatemap.payloads.SpotsQueryDTO( 
            s.id,s.name,s.latitude,s.longitude,s.city,CAST(s.continents AS string),s.risk,s.country,s.street,
            (SELECT m.link FROM Media m WHERE m.spot = s AND TYPE(m) = Image ORDER BY m.id ASC LIMIT 1),s.status)
            FROM Spot s  ORDER BY s.id ASC
    """)
    List<SpotsQueryDTO> findAllStatusForList();
    @Query("""
    SELECT 
        new fra.skatemap.payloads.SpotsQueryDTO( 
            s.id,s.name,s.latitude,s.longitude,s.city,CAST(s.continents AS string),s.risk,s.country,s.street,
            (SELECT m.link FROM Media m WHERE m.spot = s AND TYPE(m) = Image ORDER BY m.id ASC LIMIT 1),s.status)
            FROM Spot s WHERE s.user.id = :userId  ORDER BY s.id ASC
    """)
    List<SpotsQueryDTO> findAllMyStatusForList(UUID userId);
    @Query("""
    SELECT s.id, st.type.spotType
    FROM Spot s
    JOIN s.spotTypes st
    """)
    List<Object[]> findAllStatusSpotTypes();
}