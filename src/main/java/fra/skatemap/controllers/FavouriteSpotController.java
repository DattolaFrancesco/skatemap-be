package fra.skatemap.controllers;

import fra.skatemap.entities.FavouriteSpot;
import fra.skatemap.entities.User;
import fra.skatemap.payloads.FavouriteSpotResponseDTO;
import fra.skatemap.payloads.SpotMinimalResponseDTO;
import fra.skatemap.payloads.SpotResponseDTO;
import fra.skatemap.services.FavouriteSpotService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/fav")
public class FavouriteSpotController {
    private final FavouriteSpotService favouriteSpotService;

    public FavouriteSpotController(FavouriteSpotService favouriteSpotService) {
        this.favouriteSpotService = favouriteSpotService;
    }

    @PostMapping("/{spotId}")
    public FavouriteSpotResponseDTO save(@PathVariable UUID spotId, @AuthenticationPrincipal User user){
        return this.favouriteSpotService.save(spotId, user);
    }
    @GetMapping("/{spotId}")
    public SpotResponseDTO findSingleFav(@PathVariable UUID spotId, @AuthenticationPrincipal User user){
        return this.favouriteSpotService.findSingleFav(spotId, user);
    }
    @GetMapping("/all")
    public Page<SpotMinimalResponseDTO> findFav(@AuthenticationPrincipal User user,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "500") int size,
                                                @RequestParam(defaultValue = "spot.name") String sortBy){
        return this.favouriteSpotService.findFav(user, page, size, sortBy);
    }
    @DeleteMapping("/{spotId}")
    public void deleteBySpotIdAndUserId(@PathVariable UUID spotId, @AuthenticationPrincipal User user){
        this.favouriteSpotService.deleteBySpotIdAndUserId(spotId,user);
    }
}
