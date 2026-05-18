package fra.skatemap.entities;

import fra.skatemap.enums.Status_spot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "spots")
@Getter
@Setter
@NoArgsConstructor
public class Spot {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private double latitude;
    @Column(nullable = false)
    private double longitude;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status_spot status;
    @Column(nullable = false)
    private String risk;
    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Media> media = new ArrayList<>();
    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<SpotType> spotTypes;

    public Spot(String description, double latitude, double longitude, String name, String risk) {
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.risk = risk;
        this.status = Status_spot.PENDING;
    }
}
