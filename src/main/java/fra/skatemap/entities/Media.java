package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "media_type")
@Getter
@Setter
@NoArgsConstructor
public class Media {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String link;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    public Media(Spot spot, String link) {
        this.spot = spot;
        this.link = link;
    }
}
