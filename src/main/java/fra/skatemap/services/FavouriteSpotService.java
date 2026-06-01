package fra.skatemap.services;

import fra.skatemap.entities.FavouriteSpot;
import fra.skatemap.entities.Image;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.User;
import fra.skatemap.payloads.FavouriteSpotResponseDTO;
import fra.skatemap.payloads.SpotMinimalResponseDTO;
import fra.skatemap.payloads.SpotResponseDTO;
import fra.skatemap.repositories.FavouriteSpotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FavouriteSpotService {
    private final FavouriteSpotRepository favouriteSpotRepository;
    private final SpotService spotService;
    private final UsersService usersService;

    public FavouriteSpotService(FavouriteSpotRepository favouriteSpotRepository, SpotService spotService, UsersService usersService) {
        this.favouriteSpotRepository = favouriteSpotRepository;
        this.spotService = spotService;
        this.usersService = usersService;
    }
    private void checkCredentials(UUID spotId, User user){
        Spot spot = this.spotService.findSpotById(spotId);
        User user1 = this.usersService.findById(user.getId());
    }
    public FavouriteSpotResponseDTO save(UUID spotId, User user){
        checkCredentials(spotId,user);
        FavouriteSpot favouriteSpot = this.favouriteSpotRepository.save
                (new FavouriteSpot(this.spotService.findSpotById(spotId),user));
        return  new FavouriteSpotResponseDTO(favouriteSpot.getSpot().getName(),favouriteSpot.getUser().getName());
    }
    public SpotResponseDTO findSingleFav(UUID spotId, User user){
        checkCredentials(spotId,user);
         FavouriteSpot favouriteSpot = this.favouriteSpotRepository.findBySpotIdAndUserId(spotId,user.getId());
        return this.spotService.toDTO(favouriteSpot.getSpot());
    }
    public Page<SpotMinimalResponseDTO> findFav(User user, int page, int size, String sortBy){
        User user1 = this.usersService.findById(user.getId());
        Pageable pageable = PageRequest.of(page,size,Sort.by(sortBy));
        return this.favouriteSpotRepository.findByUserId(user1.getId(), pageable)
                .map(s-> new SpotMinimalResponseDTO(
                        s.getSpot().getId(),s.getSpot().getName(),s.getSpot().getLatitude(),s.getSpot().getLongitude(),s.getSpot().getCity(),
                        s.getSpot().getMedia().stream().filter(m->m instanceof Image).toList().getFirst()));
    }
    public void deleteBySpotIdAndUserId(UUID spotId, User user){
     checkCredentials(spotId,user);
     FavouriteSpot favouriteSpot = this.favouriteSpotRepository.findBySpotIdAndUserId(spotId,user.getId());
     this.favouriteSpotRepository.deleteById(favouriteSpot.getId());
    }

}
