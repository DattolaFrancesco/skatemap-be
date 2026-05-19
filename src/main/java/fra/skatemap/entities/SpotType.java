package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "spot_types")
@Getter
@Setter
@NoArgsConstructor
public class SpotType {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private Type type;

    public SpotType(Spot spot, Type type) {
        this.spot = spot;
        this.type = type;
    }
}
