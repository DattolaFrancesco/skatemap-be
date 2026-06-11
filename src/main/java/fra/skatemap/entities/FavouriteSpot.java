package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "favourite_spots")
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public Spot getSpot() {
        return spot;
    }

    public User getUser() {
        return user;
    }

    public FavouriteSpot() {
    }
    public FavouriteSpot(Spot spot, User user) {
        this.spot = spot;
        this.user = user;
    }
}
