package fra.skatemap.payloads;

import jakarta.validation.constraints.NotBlank;

public record RolesDTO(
        @NotBlank(message = "role can't be blank")
        String roleName
) {
}
