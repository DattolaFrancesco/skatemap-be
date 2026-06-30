package fra.skatemap.services;

import fra.skatemap.entities.*;
import fra.skatemap.enums.Continents;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.*;
import fra.skatemap.repositories.SpotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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
        spot.setStatus(Status_spot.PENDING);
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
    public Page<SpotResponseOwnDTO> filterMinimalSpots(Specification<Spot> spot, int page, int size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Spot> spots = this.spotRepository.findAll(spot,pageable);
        return spots.map(s-> new SpotResponseOwnDTO(
                s.getId(),s.getName(),s.getLatitude(),s.getLongitude(),s.getCity(),
                s.getMedia().stream().filter(m->m instanceof Image).findFirst().orElse(null),s.getStatus()));
    }
    public Page<SpotResponseOwnDTO> getOwnSpots(User user, int page, int size, String sortBy, String status){
        if(user == null) throw new BadRequestException("user not authenticated");
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        String rightStatus = status.toUpperCase();
        boolean validStatus = status != null && !status.isBlank() &&
                (!status.equals("PENDING") && !status.equals("APPROVED") && !status.equals("UNAPPROVED"));
        if (!validStatus)return this.spotRepository.findByUserId(user.getId(),pageable).map(s-> new SpotResponseOwnDTO(
                s.getId(),s.getName(),s.getLatitude(),s.getLongitude(),s.getCity(),
                s.getMedia().stream().filter(m->m instanceof Image).findFirst().orElse(null),s.getStatus()));
        else{
            Status_spot statusSpot = switch (rightStatus){
                case "PENDING" -> Status_spot.PENDING;
                case "APPROVED" -> Status_spot.APPROVED;
                case "UNAPPROVED" -> Status_spot.UNAPPROVED;
                default -> Status_spot.APPROVED;
            };
            return this.spotRepository.findByStatusAndUserId(statusSpot,user.getId(),pageable).map(s-> new SpotResponseOwnDTO(
                    s.getId(),s.getName(),s.getLatitude(),s.getLongitude(),s.getCity(),
                    s.getMedia().stream().filter(m->m instanceof Image).toList().getFirst(),s.getStatus()));
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

    // ============================================================
    // FIX (modifyAll):
    // 1. Il loop di classificazione image/video e' ora DENTRO il
    //    metodo @Transactional cosi' come prima, ma soprattutto
    //    qualsiasi eccezione al suo interno fa scattare il rollback
    //    automatico di Spring invece di lasciare lo spot/i media
    //    in uno stato a meta'.
    // 2. Il controllo "contentType == null" e' separato da quello
    //    GIF: prima un content-type non leggibile (capita spesso
    //    su iOS/Safari per certi video) faceva uscire il messaggio
    //    fuorviante "GIF files are not supported".
    // 3. Rimosso il try/catch manuale con "this.deleteById(...)":
    //    non serve piu', @Transactional fa rollback da solo su
    //    qualunque RuntimeException lanciata nel metodo.
    @Transactional
    public String modifyAll(UUID id, ModifiedSpotDTO modifiedSpotDTO, List<MultipartFile> files){
        Spot spot = findSpotById(id); // find spot
        // delete existing media  that need to go
        modifiedSpotDTO.eliminatedMedia().forEach(m->this.mediaService.deleteById(UUID.fromString(m)));
        // get if the media are video or img
        List<MultipartFile> image = new ArrayList<>();
        List<MultipartFile> video = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile medias : files) {
                String contentType = medias.getContentType();
                if (contentType == null) {
                    throw new BadRequestException(
                            "Could not determine file type for '" + medias.getOriginalFilename() + "'");
                }
                if (contentType.startsWith("image/gif")) {
                    throw new BadRequestException("GIF files are not supported");
                }
                if (contentType.startsWith("image")) {
                    image.add(medias);
                }
                else if (contentType.startsWith("video")) {
                    video.add(medias);
                }
                else throw new BadRequestException("Unsupported file type: " + contentType);
            }
        }
        if (!image.isEmpty()) mediaService.saveImage(spot, image);
        if (!video.isEmpty()) mediaService.saveVideo(spot, video);

        modifyById(id,new SpotRequestDTO(
                modifiedSpotDTO.name(), modifiedSpotDTO.latitude(), modifiedSpotDTO.longitude(),
                modifiedSpotDTO.description(), modifiedSpotDTO.risk(),modifiedSpotDTO.continent()
                , modifiedSpotDTO.country(), modifiedSpotDTO.city(), modifiedSpotDTO.street(),
                modifiedSpotDTO.types()));
        return modifiedSpotDTO.name() + " modified with success";

    }

    // ============================================================
    // FIX (saveAll): stesso identico discorso di modifyAll sopra.
    // Prima, se il loop di classificazione lanciava un'eccezione
    // (es. content-type null su un video, o un file non supportato),
    // lo spot appena creato restava salvato per sempre senza nessun
    // media collegato, perche' quell'eccezione usciva PRIMA del
    // blocco try/catch che faceva il cleanup. Ora il metodo intero
    // e' @Transactional: qualunque eccezione, in qualunque punto,
    // fa rollback automatico di spot + tipi + media insieme.
    @Transactional
    public void saveAll(SpotRequestDTO spot,List<MultipartFile> media,User user){
        if(media == null || media.isEmpty()) throw new BadRequestException("we need at least one image!");
        Spot newSpot = this.save(spot,user);
        List<MultipartFile> image = new ArrayList<>();
        List<MultipartFile> video = new ArrayList<>();
        for (MultipartFile medias : media) {
            String contentType = medias.getContentType();
            if (contentType == null) {
                throw new BadRequestException(
                        "Could not determine file type for '" + medias.getOriginalFilename() + "'");
            }
            if (contentType.startsWith("image/gif")) {
                throw new BadRequestException("GIF files are not supported");
            }
            if (contentType.startsWith("image")) {
                image.add(medias);
            }
            else if (contentType.startsWith("video")) {
                video.add(medias);
            }
            else throw new BadRequestException("Unsupported file type: " + contentType);
        }
        if (!image.isEmpty()) mediaService.saveImage(newSpot, image);
        if (!video.isEmpty()) mediaService.saveVideo(newSpot, video);
    }

    public List<SpotListResponseDTO> findListAll(String status){
        Status_spot statusSpot = Status_spot.valueOf(status.toUpperCase());
        List<SpotsQueryDTO> spots = this.spotRepository.findAllForList((statusSpot));
        List<Object[]> typesRaw = this.spotRepository.findAllSpotTypes(statusSpot);
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
    public List<SpotListResponseDTO> findListAllStatus(){
        List<SpotsQueryDTO> spots = this.spotRepository.findAllStatusForList();
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
    public List<SpotListResponseDTO> findListAllMyStatus(User user){
        if(user == null) throw new BadRequestException("user not authenticated");
        List<SpotsQueryDTO> spots = this.spotRepository.findAllMyStatusForList(user.getId());
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
}