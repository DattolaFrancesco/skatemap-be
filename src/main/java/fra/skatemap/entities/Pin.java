package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "Pins")
@Getter
@Setter
@NoArgsConstructor
public class Pin {
    @Id
    @GeneratedValue
    @JsonIgnore
    private UUID id;
    private double latitude;
    private double longitude;

    public Pin(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
