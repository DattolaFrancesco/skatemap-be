package fra.skatemap.payloads;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;


public record PinDTO(
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        @Digits(integer = 2, fraction = 1)
        double latitudine,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        @Digits(integer = 3, fraction = 1)
        double longitude
){
}
