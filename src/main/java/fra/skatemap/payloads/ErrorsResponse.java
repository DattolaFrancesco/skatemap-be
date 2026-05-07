package fra.skatemap.payloads;

import java.time.LocalDateTime;

public record ErrorsResponse(
        String message,
        LocalDateTime timestamp
) {
}