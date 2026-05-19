package fra.skatemap.services;

import fra.skatemap.entities.Spot;
import fra.skatemap.entities.SpotType;
import fra.skatemap.entities.Type;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.repositories.SpotTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class SpotTypeService {
    private final SpotTypeRepository spotTypeRepository;

    public SpotTypeService(SpotTypeRepository spotTypeRepository) {
        this.spotTypeRepository = spotTypeRepository;
    }

    public SpotType save(Spot spot, Type type){
        if(this.spotTypeRepository.existsBySpotIdAndTypeId(spot.getId(),type.getId())) throw new BadRequestException("spot type already in the db");
        else  return this.spotTypeRepository.save(new SpotType(spot,type));
    }
    public List<SpotType> findBySpotId(UUID id){
       return this.spotTypeRepository.findBySpotId(id);
    }
    public void deleteById(UUID id){
        this.spotTypeRepository.deleteById(id);
    }
}
