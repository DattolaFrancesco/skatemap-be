package fra.skatemap.entities;

import fra.skatemap.enums.Continents;
import fra.skatemap.enums.Status_spot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "spots")
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
    @Column(nullable = false, length = 500)
    private String description;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status_spot status;
    @Column(nullable = false)
    private String risk;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Continents continents;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String street;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "spot")
    private List<Media> media = new ArrayList<>();
    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SpotType> spotTypes = new HashSet<>();
    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavouriteSpot> favourites = new ArrayList<>();

    public String getCity() {
        return city;
    }

    public Continents getContinents() {
        return continents;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public List<FavouriteSpot> getFavourites() {
        return favourites;
    }

    public UUID getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Media> getMedia() {
        return media;
    }

    public String getName() {
        return name;
    }

    public String getRisk() {
        return risk;
    }

    public Set<SpotType> getSpotTypes() {
        return spotTypes;
    }

    public Status_spot getStatus() {
        return status;
    }

    public String getStreet() {
        return street;
    }

    public User getUser() {
        return user;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setContinents(Continents continents) {
        this.continents = continents;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFavourites(List<FavouriteSpot> favourites) {
        this.favourites = favourites;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public void setSpotTypes(Set<SpotType> spotTypes) {
        this.spotTypes = spotTypes;
    }

    public void setStatus(Status_spot status) {
        this.status = status;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Spot(String description, double latitude, double longitude, String name,
                String risk, User user, Continents continents, String country, String city, String street) {
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.risk = risk;
        this.continents = continents;
        this.country = country;
        this.city = city;
        this.street = street;
        this.status = Status_spot.PENDING;
        this.user = user;
    }

}
