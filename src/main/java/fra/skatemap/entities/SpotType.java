package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "spot_types")
public class SpotType {
    @Id
    @GeneratedValue
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private Type type;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public Spot getSpot() {
        return spot;
    }

    public Type getType() {
        return type;
    }

    public  SpotType(){}
    public SpotType(Spot spot, Type type) {
        this.spot = spot;
        this.type = type;
    }
}
