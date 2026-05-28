package fra.skatemap.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ModifiedSpotDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 30, message = "Name must be at most 30 characters")
        String name,
        @NotNull(message = "Latitude is required")
        Double latitude,
        @NotNull(message = "Longitude is required")
        Double longitude,
        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "description must be at most 500 characters")
        String description,
        @NotBlank(message = "Risk is required")
        String risk,
        @NotBlank(message = "Continent is required")
        String continent,
        @NotBlank(message = "Country is required")
        String country,
        @NotBlank(message = "City is required")
        String city,
        @NotBlank(message = "Street is required")
        String street,
        @NotNull(message = "Types is required")
        @NotEmpty(message = "Types is required")
        List<String> types,
        List<String> eliminatedMedia
) {
}
