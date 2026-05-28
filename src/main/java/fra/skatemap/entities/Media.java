package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "media_type")
@NoArgsConstructor
public class Media {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String link;
    @Column(nullable = false)
    private String publicId;
    @Column(nullable = false)
    private String format;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    public void setFormat(String format) {
        this.format = format;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public String getFormat() {
        return format;
    }

    public UUID getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getPublicId() {
        return publicId;
    }

    public Spot getSpot() {
        return spot;
    }

    public Media() {
   
    }

    public Media(Spot spot, String link, String publicId, String format) {
        this.spot = spot;
        this.link = link;
        this.publicId = publicId;
        this.format = format;
    }
}
