package fra.skatemap.services;

import com.cloudinary.utils.ObjectUtils;
import fra.skatemap.config.CloudinaryConfig;
import fra.skatemap.entities.Image;
import fra.skatemap.entities.Media;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.Video;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.repositories.MediaRepository;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final CloudinaryConfig cloudinaryConfig;
    private final SpotService spotService;

    public MediaService(MediaRepository mediaRepository, CloudinaryConfig cloudinaryConfig, SpotService spotService) {
        this.mediaRepository = mediaRepository;
        this.cloudinaryConfig = cloudinaryConfig;
        this.spotService = spotService;
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
    public void saveImage(UUID spotId, List<MultipartFile> files){
        if (files == null || files.isEmpty()) {
            return;
        }
        Spot spot = this.spotService.findSpotById(spotId);
        if(this.mediaRepository.countBySpot(spot)+ files.size()>5) throw new BadRequestException("the maximum photo for every spot is 10");
        Tika tika = new Tika();
        if(files != null && !files.isEmpty()){
            for (MultipartFile file : files) {
                try {
                    String type = tika.detect(file.getInputStream());
                    if (!type.startsWith("image")) {
                        throw new BadRequestException("files aren't image");
                    }
                } catch (IOException e) {
                    throw new BadRequestException("File is corrupted or unreadable");
                }
                String url = uploadImage(file);
                Media media = new Image(spot,url);
                this.mediaRepository.save(media);
            }
        }
    }
    public void saveVideo(UUID spotId, List<MultipartFile> files){
        if (files == null || files.isEmpty()) {
            return;
        }
        Spot spot = this.spotService.findSpotById(spotId);
        if(this.mediaRepository.countBySpot(spot)+ files.size()>5) throw new BadRequestException("the maximum photo for every spot is 10");
        Tika tika = new Tika();
        if(files != null && !files.isEmpty()){
            for (MultipartFile file : files) {
                try {
                    String type = tika.detect(file.getInputStream());
                    if (!type.startsWith("video")) {
                        throw new BadRequestException("files aren't video");
                    }
                } catch (IOException e) {
                    throw new BadRequestException("File is corrupted or unreadable");
                }
                String url = uploadVideo(file);
                Media media = new Video(spot,url);
                this.mediaRepository.save(media);
            }
        }
    }
    public Media findById(UUID id){
        return this.mediaRepository.findById(id).orElseThrow(()-> new NotFoundException("media not found"));
    }
    public void deleteById(UUID id){
        findById(id);
        this.mediaRepository.deleteById(id);
    }
    public Page<Media> findAllMediaByIdAndType(UUID id,String type, int page, int size ){
        Pageable pageable = PageRequest.of(page, size);
        if(type == null ) return  this.mediaRepository.findBySpotId(id,pageable);
        switch (type.toLowerCase()) {
            case "image":
                return this.mediaRepository.findBySpotAndType(id,Image.class, pageable);
            case "video":
                return this.mediaRepository.findBySpotAndType(id,Video.class, pageable);
            default:
                return this.mediaRepository.findBySpotId(id,pageable);
        }
    }

}
