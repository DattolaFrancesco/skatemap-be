package fra.skatemap.services;

import fra.skatemap.entities.Type;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.TypeDTO;
import fra.skatemap.repositories.TypeRepository;
import org.springframework.stereotype.Service;

@Service
public class TypeService {
    private final TypeRepository typeRepository;

    public TypeService(TypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }
    public Type save(TypeDTO typeDTO){
        if(this.typeRepository.existsBySpotType(typeDTO.spotType())) throw new BadRequestException("this type already exist");
        else return this.typeRepository.save(new Type(typeDTO.spotType()));
    }
    public Type findByName(String type){
        return this.typeRepository.findBySpotType(type).orElseThrow(()-> new BadRequestException("this type doesn't exist"));
    }
}
