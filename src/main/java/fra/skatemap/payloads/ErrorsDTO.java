package fra.skatemap.payloads;

import java.time.LocalDateTime;

public record ErrorsDTO(String message, LocalDateTime timestamp) {
}
