package fra.skatemap.payloads;

import java.time.LocalDate;

public record TokenResponseDTO(String message, LocalDate timeStamp) {
}
