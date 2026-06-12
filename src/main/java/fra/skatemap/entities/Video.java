package fra.skatemap.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("video")
public class Video extends Media {
    private String thumbnailUrl;
    private String status;
    public Video() {}

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Video(Spot spot, String url, String publicId, String thumbnailUrl) {
        super(spot, url, publicId,"video");
        this.thumbnailUrl = thumbnailUrl;
        this.status = "PENDING";
    }
}