package fra.skatemap.payloads;

import fra.skatemap.enums.Status_spot;

import java.util.List;
import java.util.UUID;

public record SpotResponseDTO(
        UUID id,
        String name,
        String description,
        double latitude,
        double longitude,
        String risk,
        Status_spot status,
        List<String> spotTypes,
        List<String> images,
        List<String> videos
) {}
