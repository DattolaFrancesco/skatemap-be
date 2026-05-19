package fra.skatemap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "types_of_spot")
@Getter
@Setter
@NoArgsConstructor
public class Type {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "spot_type", nullable = false)
    private String spotType;

    public Type(String spotType) {
        this.spotType = spotType;
    }
}

