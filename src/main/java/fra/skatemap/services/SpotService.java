package fra.skatemap.services;

import fra.skatemap.entities.*;
import fra.skatemap.enums.Continents;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.*;
import fra.skatemap.repositories.SpotRepository;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
        Continents c = Continents.AFRICA ;
        if(spotRequestDTO.continent() != null){
          String continent = spotRequestDTO.continent().toLowerCase();
            c = switch (continent) {
                case "africa" -> Continents.AFRICA;
                case "asia" -> Continents.ASIA;
                case "europe" -> Continents.EUROPE;
                case "north america" -> Continents.NORTHAMERICA;
                case "south america" -> Continents.SOUTHAMERICA;
                case "oceania" -> Continents.OCEANIA;
                case "antartica" -> Continents.ANTARTICA;
                default -> c;
            };
        }
        Spot spot = new Spot(spotRequestDTO.description(), spotRequestDTO.latitude(),
                spotRequestDTO.longitude(), spotRequestDTO.name()
                , spotRequestDTO.risk().toUpperCase()
                ,user,c, spotRequestDTO.country().toUpperCase(),spotRequestDTO.city().toUpperCase(),spotRequestDTO.street().toUpperCase());
        this.spotRepository.save(spot);
        List<Type> types = spotRequestDTO.types().stream()
                .map(name->this.typeService.findByName(name.toUpperCase())).toList();
        for(Type type : types){
            this.spotTypeService.save(spot,type);
        }
        return spot;
    }
    public SpotResponseDTO toDTO(Spot spot) {
        List<Media> allMedia = spot.getMedia();
        return new SpotResponseDTO(
                spot.getId(),
                spot.getName(),
                spot.getDescription(),
                spot.getLatitude(),
                spot.getLongitude(),
                spot.getContinents().toString(),
                spot.getCountry(),
                spot.getCity(),
                spot.getStreet(),
                spot.getRisk(),
                spot.getStatus(),
                spot.getSpotTypes().stream()
                        .map(st -> st.getType().getSpotType())
                        .toList(),
                allMedia.stream().filter(s -> s instanceof Video).toList(),
                allMedia.stream().filter(s -> s instanceof Image).toList()
        );
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
        spot.setRisk(body.risk().toUpperCase());
        spot.setCountry(body.country().toUpperCase());
        spot.setCity(body.city().toUpperCase());
        spot.setStreet(body.street().toUpperCase());
        if(body.continent() != null){
            Continents c = Continents.AFRICA ;
            String continent = body.continent().toLowerCase();
            c = switch (continent) {
                case "africa" -> Continents.AFRICA;
                case "asia" -> Continents.ASIA;
                case "europe" -> Continents.EUROPE;
                case "northamerica" -> Continents.NORTHAMERICA;
                case "southamerica" -> Continents.SOUTHAMERICA;
                case "oceania" -> Continents.OCEANIA;
                case "antartica" -> Continents.ANTARTICA;
                default -> c;
            };
            spot.setContinents(c);
        }
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
    public Page<SpotResponseDTO> filterSpots(Specification<Spot> spot, int page, int size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Spot> spots = this.spotRepository.findAll(spot,pageable);
        return spots.map(s-> toDTO(s));
    }
    public Page<SpotMinimalResponseDTO> filterMinimalSpots(Specification<Spot> spot, int page, int size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Spot> spots = this.spotRepository.findAll(spot,pageable);
        return spots.map(s-> new SpotMinimalResponseDTO(
                s.getId(),s.getName(),s.getLatitude(),s.getLongitude(),s.getCity(),
                s.getMedia().stream().filter(m->m instanceof Image).findFirst().orElse(null)));
    }
    public Page<SpotMinimalResponseDTO> getOwnSpots(User user, int page, int size, String sortBy, String status){
            if(user == null) throw new BadRequestException("user not authenticated");
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            String rightStatus = status.toUpperCase();
            boolean validStatus = status != null && !status.isBlank() &&
                    (!status.equals("PENDING") && !status.equals("APPROVED") && !status.equals("UNAPPROVED"));
            if (!validStatus)return this.spotRepository.findByUserId(user.getId(),pageable).map(s-> new SpotMinimalResponseDTO(
                    s.getId(),s.getName(),s.getLatitude(),s.getLongitude(),s.getCity(),
                    s.getMedia().stream().filter(m->m instanceof Image).findFirst().orElse(null)));
            else{
                 Status_spot statusSpot = switch (rightStatus){
                    case "PENDING" -> Status_spot.PENDING;
                    case "APPROVED" -> Status_spot.APPROVED;
                    case "UNAPPROVED" -> Status_spot.UNAPPROVED;
                     default -> Status_spot.APPROVED;
                };
                return this.spotRepository.findByStatusAndUserId(statusSpot,user.getId(),pageable).map(s-> new SpotMinimalResponseDTO(
                        s.getId(),s.getName(),s.getLatitude(),s.getLongitude(),s.getCity(),
                        s.getMedia().stream().filter(m->m instanceof Image).toList().getFirst()));
            }
    }
    public Page<SpotMinimalResponseDTO> getPendingSpots( int page, int size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return this.spotRepository.findByStatus(Status_spot.PENDING,pageable).map(s-> new SpotMinimalResponseDTO(
                s.getId(),s.getName(),s.getLatitude(),s.getLongitude(),s.getCity(),
                s.getMedia().stream().filter(m->m instanceof Image).findFirst().orElse(null)));
    }
    public SpotResponseDTO modifyStatus(UUID id, String status) {
        Spot spot = this.spotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Spot not found"));
        spot.setStatus(Status_spot.valueOf(status.toUpperCase()));
        return toDTO(this.spotRepository.save(spot));
    }
    @Transactional
    public String modifyAll(UUID id, ModifiedSpotDTO modifiedSpotDTO, List<MultipartFile> files){
        Spot spot = findSpotById(id); // find spot
        // delete existing media  that need to go
        modifiedSpotDTO.eliminatedMedia().forEach(m->this.mediaService.deleteById(UUID.fromString(m)));
        // get if the media are video or img
        List<MultipartFile> image = new ArrayList<>();
        List<MultipartFile> video = new ArrayList<>();
        Tika tika = new Tika();
        if (files != null && !files.isEmpty()) {
        for (MultipartFile file : files) {
            try {
                String mimeType = tika.detect(file.getInputStream());
                if (mimeType != null && mimeType.startsWith("image/gif")) {
                    throw new BadRequestException("GIF files are not supported");
                }
                if (mimeType.startsWith("image")) {
                    image.add(file);
                } else if (mimeType.startsWith("video")) {
                    video.add(file);
                } else {
                    throw new BadRequestException("Unsupported file type: " + mimeType);
                }

            } catch (IOException e) {
                throw new BadRequestException("File is corrupted or unreadable");
            }
        }}
        // saving new imgs
        if (!image.isEmpty()) {
            mediaService.saveImage(spot, image);
        }
        if (!video.isEmpty()) {
            mediaService.saveVideo(spot, video);
        }
        //save new info in spot
        modifyById(id,new SpotRequestDTO(
                modifiedSpotDTO.name(), modifiedSpotDTO.latitude(), modifiedSpotDTO.longitude(),
                modifiedSpotDTO.description(), modifiedSpotDTO.risk(),modifiedSpotDTO.continent()
                , modifiedSpotDTO.country(), modifiedSpotDTO.city(), modifiedSpotDTO.street(),
                modifiedSpotDTO.types()));
        return modifiedSpotDTO.name() + " modified with success";

    }
    public void saveAll(SpotRequestDTO spot,List<MultipartFile> media,User user){
        if(media == null || media.isEmpty()) throw new BadRequestException("we need at least one image!");
        Spot newSpot = this.save(spot,user);
        List<MultipartFile> image = new ArrayList<>();
        List<MultipartFile> video = new ArrayList<>();
        Tika tika = new Tika();
        if (media != null && !media.isEmpty()) {
            for (MultipartFile medias : media) {
                try {
                    String mimeType = tika.detect(medias.getInputStream());
                    if (mimeType != null && mimeType.startsWith("image/gif")) {
                        throw new BadRequestException("GIF files are not supported");
                    }
                    if (mimeType.startsWith("image")) {
                        image.add(medias);
                    } else if (mimeType.startsWith("video")) {
                        video.add(medias);
                    } else {
                        throw new BadRequestException("Unsupported file type: " + mimeType);
                    }
                } catch (IOException e) {
                    throw new BadRequestException("File is corrupted or unreadable");
                }
            }}
        try {
            if (!image.isEmpty()) mediaService.saveImage(newSpot, image);
            if (!video.isEmpty()) mediaService.saveVideo(newSpot, video);
        } catch (Exception e) {
            this.deleteById(newSpot.getId());
            throw new BadRequestException(e.getMessage());
        }
    }
}
