package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("video")
public class Video extends Media {

    public Video() {}

    public Video(Spot spot, String url, String publicId) {
        super(spot, url, publicId,"video");
    }
}