package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("video")
@Getter
@Setter
public class Video extends Media {
    private String thumbnailUrl;
    public Video() {}

    public Video(Spot spot, String url, String publicId, String thumbnailUrl) {
        super(spot, url, publicId,"video");
        this.thumbnailUrl = thumbnailUrl;
    }
}