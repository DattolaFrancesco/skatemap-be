package fra.skatemap.payloads;

import fra.skatemap.enums.Status_spot;

import java.util.List;
import java.util.UUID;

public record SpotListResponseDTO(
        UUID id,
        String name,
        double latitude,
        double longitude,
        String city,
        String continent,
        String risk,
        String country,
        List<String> spotTypes,
        String thumbnailUrl,
        Status_spot status
) {
}
