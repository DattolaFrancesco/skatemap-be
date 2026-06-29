package fra.skatemap.controllers;

import fra.skatemap.entities.FavouriteSpot;
import fra.skatemap.entities.User;
import fra.skatemap.payloads.*;
import fra.skatemap.services.FavouriteSpotService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<SpotListResponseDTO> findFav(@AuthenticationPrincipal User user){
        return this.favouriteSpotService.findFav(user);
    }
    @DeleteMapping("/{spotId}")
    public void deleteBySpotIdAndUserId(@PathVariable UUID spotId, @AuthenticationPrincipal User user){
        this.favouriteSpotService.deleteBySpotIdAndUserId(spotId,user);
    }
}
