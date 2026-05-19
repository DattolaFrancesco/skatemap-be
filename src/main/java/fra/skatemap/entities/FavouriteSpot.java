package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "favourite_spots")
@Getter
@Setter
@NoArgsConstructor
public class FavouriteSpot {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    public FavouriteSpot(Spot spot, User user) {
        this.spot = spot;
        this.user = user;
    }
}
