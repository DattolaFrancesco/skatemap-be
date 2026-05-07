package fra.skatemap.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UsersDTO(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
                , message = "Password must contain at least 8 characters," +
                " one uppercase, one lowercase, one number and one special character")
        String password,
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Surname is required")
        String surname,
        @NotBlank(message = "Avatar is required")
        String avatar,
        @NotBlank(message = "Avatar is required")
        String role
) {
}