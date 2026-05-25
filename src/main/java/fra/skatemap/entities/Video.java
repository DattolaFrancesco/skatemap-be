package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("video")
public class Video extends Media {
    private String ThumbnailUrl;
    public Video() {}

    public Video(Spot spot, String url, String publicId, String thumbnailUrl) {
        super(spot, url, publicId,"video");
        this.ThumbnailUrl = thumbnailUrl;
    }
}