package fra.skatemap.services;

import com.cloudinary.utils.ObjectUtils;
import fra.skatemap.config.CloudinaryConfig;
import fra.skatemap.entities.Image;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.Type;
import fra.skatemap.entities.Video;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.SpotRequestDTO;
import fra.skatemap.payloads.SpotResponseDTO;
import fra.skatemap.repositories.ImageRepository;
import fra.skatemap.repositories.SpotRepository;
import fra.skatemap.repositories.SpotTypeRepository;
import fra.skatemap.repositories.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SpotService {
    private final CloudinaryConfig cloudinaryConfig;
    private final SpotRepository spotRepository;
    private final ImageRepository imageRepository;
    private final VideoRepository videoRepository;
    private final TypeService typeService;
    private final SpotTypeService spotTypeService;

    public SpotService(CloudinaryConfig cloudinaryConfig, SpotRepository spotRepository,
                       ImageRepository imageRepository, VideoRepository videoRepository,
                       TypeService typeService, SpotTypeService spotTypeService) {
        this.cloudinaryConfig = cloudinaryConfig;
        this.spotRepository = spotRepository;
        this.imageRepository = imageRepository;
        this.videoRepository = videoRepository;
        this.typeService = typeService;
        this.spotTypeService = spotTypeService;
    }
    private String uploadImage(MultipartFile file){
        try {
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new BadRequestException("Error uploading the image");
        }
    }
    private String uploadVideo(MultipartFile file){
        try {
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "quality", "auto",
                            "fetch_format", "auto")
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new BadRequestException("Error uploading the video");
        }
    }

    @Transactional
    public Spot save(SpotRequestDTO spotRequestDTO) {
        if (spotRepository.existsByName(spotRequestDTO.name())) {
            throw new BadRequestException("A spot with this name already exists");
        }
        if (this.spotRepository.existsByLatitudeAndLongitude(spotRequestDTO.latitude(), spotRequestDTO.longitude())) {
            throw new BadRequestException("another spot has the same coordinates");
        }
        Spot spot = new Spot(spotRequestDTO.description(), spotRequestDTO.latitude(),
                spotRequestDTO.longitude(), spotRequestDTO.name()
                , spotRequestDTO.risk());
        this.spotRepository.save(spot);
        List<Type> types = spotRequestDTO.types().stream()
                .map(name->this.typeService.findByName(name)).toList();
        for(Type type : types){
            this.spotTypeService.save(spot,type);
        }
       /* if(spotRequestDTO.images() != null){
            for (MultipartFile file : spotRequestDTO.images()){
                String url = uploadImage(file);
                this.imageRepository.save(new Image(spot,url));
            }
        }
        if(spotRequestDTO.videos() != null){
            for (MultipartFile file : spotRequestDTO.videos()){
                String url = uploadVideo(file);
                this.videoRepository.save(new Video(spot,url));
            }
        }*/
        return spot;
    }
    public SpotResponseDTO findById(UUID id){
        Spot spot = this.spotRepository.findById(id).orElseThrow(()-> new BadRequestException("spot doens't exist"));
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
                spot.getImages().stream()
                        .map(Image::getLink)
                        .toList(),
                spot.getVideos().stream()
                        .map(Video::getLink)
                        .toList()
        );
    }
}
