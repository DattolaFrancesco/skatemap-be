package fra.skatemap.payloads;

import fra.skatemap.enums.Status_spot;

import java.util.UUID;

public record SpotsQueryDTO(
        UUID id,
        String name,
        double latitude,
        double longitude,
        String city,
        String continent,
        String risk,
        String country,
        String thumbnailUrl,
        Status_spot status
) {
}
