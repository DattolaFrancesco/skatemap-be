package fra.skatemap.payloads;

import fra.skatemap.entities.Media;
import fra.skatemap.enums.Status_spot;

import java.util.List;
import java.util.UUID;

public record SpotMinimalResponseDTO(
        UUID id,
        String name,
        double latitude,
        double longitude,
        String city,
        Media image
) {
}
