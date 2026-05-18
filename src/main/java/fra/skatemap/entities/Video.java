package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VIDEO")
public class Video extends Media {

    public Video() {}

    public Video(Spot spot, String url) {
        super(spot, url);
    }
}