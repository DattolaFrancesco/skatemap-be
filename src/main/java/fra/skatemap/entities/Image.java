package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("image")
public class Image extends Media {

    public Image() {}
    public Image(Spot spot, String url,String publicId) {
        super(spot, url, publicId,"image");
    }
}