package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("video")

public class Video extends Media {
    private String thumbnailUrl;
    public Video() {}

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Video(Spot spot, String url, String publicId, String thumbnailUrl) {
        super(spot, url, publicId,"video");
        this.thumbnailUrl = thumbnailUrl;
    }
}