package fra.skatemap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "spot_videos")
@Getter
@Setter
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String link;
    @ManyToOne
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    public Video(Spot spot, String link) {
        this.spot = spot;
        this.link = link;
    }
}
