package fra.skatemap.payloads;

import fra.skatemap.entities.Media;
import fra.skatemap.enums.Status_spot;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public record SpotResponseDTO(
        UUID id,
        String name,
        String description,
        double latitude,
        double longitude,
        String continents,
        String country,
        String city,
        String street,
        String risk,
        Status_spot status,
        List<String> spotTypes,
        List<Media> video,
        List<Media> image
) {}
