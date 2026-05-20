package fra.skatemap.payloads;

import fra.skatemap.enums.Status_spot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record SpotRequestDTO(
        @NotBlank(message = "Name is required")
        String name,
        @NotNull(message = "Latitude is required")
        Double latitude,
        @NotNull(message = "Longitude is required")
        Double longitude,
        @NotBlank(message = "Description is required")
        String description,
        @NotBlank(message = "Risk is required")
        String risk,
        @NotBlank(message = "Continent is required")
        String continent,
        @NotBlank(message = "City is required")
        String city,
        @NotBlank(message = "Street is required")
        String street,
        @NotNull(message = "Types is required")
        @NotEmpty(message = "Types is required")
        List<String> types
) {
}
