package fra.skatemap.services;

import fra.skatemap.entities.*;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.SpotRequestDTO;
import fra.skatemap.payloads.SpotResponseDTO;
import fra.skatemap.repositories.SpotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SpotService {
    private final SpotRepository spotRepository;
    private final TypeService typeService;
    private final SpotTypeService spotTypeService;
    private final MediaService mediaService;
    private final UsersService usersService;

    public SpotService(SpotRepository spotRepository, MediaService mediaService,
                       TypeService typeService, SpotTypeService spotTypeService,
                       UsersService usersService) {
        this.spotRepository = spotRepository;
        this.typeService = typeService;
        this.spotTypeService = spotTypeService;
        this.mediaService = mediaService;
        this.usersService = usersService;
    }

    @Transactional
    public Spot save(SpotRequestDTO spotRequestDTO, User user) {
        this.usersService.findById(user.getId());
        if (spotRepository.existsByName(spotRequestDTO.name())) {
            throw new BadRequestException("A spot with this name already exists");
        }
        if (this.spotRepository.existsByLatitudeAndLongitude(spotRequestDTO.latitude(), spotRequestDTO.longitude())) {
            throw new BadRequestException("another spot has the same coordinates");
        }
        Spot spot = new Spot(spotRequestDTO.description(), spotRequestDTO.latitude(),
                spotRequestDTO.longitude(), spotRequestDTO.name()
                , spotRequestDTO.risk(),user);
        this.spotRepository.save(spot);
        List<Type> types = spotRequestDTO.types().stream()
                .map(name->this.typeService.findByName(name)).toList();
        for(Type type : types){
            this.spotTypeService.save(spot,type);
        }
        return spot;
    }
    public SpotResponseDTO toDTO(Spot spot) {
        return new SpotResponseDTO(
                spot.getId(),
                spot.getName(),
                spot.getDescription(),
                spot.getLatitude(),
                spot.getLongitude(),
                spot.getRisk(),
                spot.getStatus(),
                spot.getSpotTypes().stream()
                        .map(st -> st.getType().getSpotType())
                        .toList(),
                spot.getMedia().stream()
                        .filter(s->s instanceof Video).toList(),
                spot.getMedia().stream()
                        .filter(s->s instanceof Image).toList());
    }
    public SpotResponseDTO findById(UUID id){
        Spot spot = this.spotRepository.findById(id).orElseThrow(()-> new BadRequestException("spot doens't exist"));
        return toDTO(spot);
    }
    public Spot findSpotById(UUID id){
        return this.spotRepository.findById(id).orElseThrow(()-> new BadRequestException("spot doens't exist"));
    }
    public Page<SpotResponseDTO> findAllSpotByStatus(Status_spot status,int page, int size, String sortBy ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        if(status !=null){
            return this.spotRepository.findByStatus(status,pageable).map(this::toDTO);
        } else return this.spotRepository.findAll(pageable).map(this::toDTO);
    }
    @Transactional
    public String deleteById(UUID id){
        SpotResponseDTO spotResponseDTO = findById(id);
        if(spotResponseDTO.image() != null && !spotResponseDTO.image().isEmpty()){
        spotResponseDTO.image().forEach(s-> {
            this.mediaService.deleteById(s.getId());
            System.out.println(s.getId());
        });
        }
        if(spotResponseDTO.video() != null && !spotResponseDTO.video().isEmpty()){
        spotResponseDTO.video().forEach(s->this.mediaService.deleteById(s.getId()));
        }
        this.spotRepository.deleteById(id);
        return spotResponseDTO.name() + " is deleted";
    }
    public SpotResponseDTO modifyById(UUID id, SpotRequestDTO body){
        Spot spot = findSpotById(id);
        spot.setName(body.name());
        spot.setLatitude(body.latitude());
        spot.setLongitude(body.longitude());
        spot.setDescription(body.description());
        spot.setRisk(body.risk());
        if (body.types() != null) {
            List<SpotType> spotTypes = this.spotTypeService.findBySpotId(id);
            spotTypes.forEach(s -> this.spotTypeService.deleteById(s.getId()));
            body.types().forEach(typeName -> {
                Type type = this.typeService.findByName(typeName);
                this.spotTypeService.save(spot, type);
            });
        }
        this.spotRepository.save(spot);
        return toDTO(spot);

    }
}
