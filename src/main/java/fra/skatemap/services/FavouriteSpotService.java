package fra.skatemap.services;

import fra.skatemap.entities.FavouriteSpot;
import fra.skatemap.entities.Image;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.User;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.*;
import fra.skatemap.repositories.FavouriteSpotRepository;
import fra.skatemap.repositories.SpotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FavouriteSpotService {
    private final FavouriteSpotRepository favouriteSpotRepository;
    private final SpotService spotService;
    private final SpotRepository spotRepository;
    private final UsersService usersService;

    public FavouriteSpotService(FavouriteSpotRepository favouriteSpotRepository,
                                SpotService spotService, UsersService usersService, SpotRepository spotRepository) {
        this.favouriteSpotRepository = favouriteSpotRepository;
        this.spotService = spotService;
        this.usersService = usersService;
        this.spotRepository = spotRepository;
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
   /* public List<SpotListResponseDTO> findFav(User user) {
        return this.favouriteSpotRepository.findAllFavForList(user.getId());
    }*/
    public List<SpotListResponseDTO> findFav(User user){
        if(user == null) throw new BadRequestException("user not authenticated");
        List<SpotsQueryDTO> spots = this.favouriteSpotRepository.findAllFavForList(user.getId());
        List<Object[]> typesRaw = this.spotRepository.findAllStatusSpotTypes();
        Map<UUID,List<String>> typesMap = new HashMap<>();
        for (Object[] row : typesRaw) {
            UUID spotId = (UUID) row[0];
            String type = (String) row[1];
            typesMap.computeIfAbsent(spotId, k -> new ArrayList<>()).add(type); // find the row if exist it add type otherwise it create the row with type
        }
        return spots.stream()
                .map(s -> new SpotListResponseDTO(
                        s.id(),
                        s.name(),
                        s.latitude(),
                        s.longitude(),
                        s.city(),
                        s.continent(),
                        s.risk(),
                        s.country(),
                        s.street(),
                        typesMap.getOrDefault(s.id(), List.of()),
                        s.thumbnailUrl(),
                        s.status()
                ))
                .toList();

    }
    public void deleteBySpotIdAndUserId(UUID spotId, User user){
     checkCredentials(spotId,user);
     FavouriteSpot favouriteSpot = this.favouriteSpotRepository.findBySpotIdAndUserId(spotId,user.getId());
     this.favouriteSpotRepository.deleteById(favouriteSpot.getId());
    }

}
