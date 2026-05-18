package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("IMAGE")
public class Image extends Media {

    public Image() {}
    public Image(Spot spot, String url) {
        super(spot, url);
    }
}