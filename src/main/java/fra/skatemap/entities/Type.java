package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "types_of_spot")
public class Type {
    @Id
    @GeneratedValue
    @JsonIgnore
    private UUID id;
    @Column(name = "spot_type", nullable = false)
    private String spotType;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSpotType(String spotType) {
        this.spotType = spotType;
    }
    public void setSpotType() {
    }

    public UUID getId() {
        return id;
    }

    public String getSpotType() {
        return spotType;
    }

    public Type(String spotType) {
        this.spotType = spotType;
    }
}

