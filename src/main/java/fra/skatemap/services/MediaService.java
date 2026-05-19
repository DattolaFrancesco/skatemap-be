package fra.skatemap.services;

import com.cloudinary.utils.ObjectUtils;
import fra.skatemap.config.CloudinaryConfig;
import fra.skatemap.entities.Image;
import fra.skatemap.entities.Media;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.Video;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.CloudinaryUploadResultDTO;
import fra.skatemap.repositories.MediaRepository;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final CloudinaryConfig cloudinaryConfig;

    public MediaService(MediaRepository mediaRepository, CloudinaryConfig cloudinaryConfig) {
        this.mediaRepository = mediaRepository;
        this.cloudinaryConfig = cloudinaryConfig;
    }
    private CloudinaryUploadResultDTO uploadImage(MultipartFile file){
        try {
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );
            return new CloudinaryUploadResultDTO(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString(),
                    uploadResult.get("resource_type").toString()
                    );
        } catch (IOException e) {
            throw new BadRequestException("Error uploading the image");
        }
    }
    private CloudinaryUploadResultDTO uploadVideo(MultipartFile file){
        try {
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "quality", "auto",
                            "fetch_format", "auto")
            );
            return new CloudinaryUploadResultDTO(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString(),
                    uploadResult.get("resource_type").toString()
            );
        } catch (IOException e) {
            throw new BadRequestException("Error uploading the video");
        }
    }
    @Transactional
    public void saveImage(Spot spot, List<MultipartFile> files){
        if (files == null || files.isEmpty()) {
            return;
        }
        Tika tika = new Tika();
        for (MultipartFile file : files) {
            try {
                String mimeType = tika.detect(file.getBytes());
                if (!mimeType.startsWith("image")) {
                    throw new BadRequestException("Files aren't images");
                }
            } catch (IOException e) {
                throw new BadRequestException("File is corrupted or unreadable");
            }
            CloudinaryUploadResultDTO body = uploadImage(file);
            Media media = new Image(spot,body.url(),body.publicId());
            this.mediaRepository.save(media);
        }
    }
    @Transactional
    public void saveVideo(Spot spot, List<MultipartFile> files){
        if (files == null || files.isEmpty()) {
            return;
        }
        if(this.mediaRepository.countBySpot(spot)+ files.size()>5) throw new BadRequestException("the maximum photo for every spot is 10");
        Tika tika = new Tika();
        for (MultipartFile file : files) {
            try {
                String mimeType = tika.detect(file.getBytes());
                if (!mimeType.startsWith("video")) {
                    throw new BadRequestException("Files aren't video");
                }
            } catch (IOException e) {
                throw new BadRequestException("File is corrupted or unreadable");
            }
            CloudinaryUploadResultDTO body = uploadVideo(file);
            Media media = new Video(spot,body.url(),body.publicId());
            this.mediaRepository.save(media);
        }
    }
    public Media findById(UUID id){
        return this.mediaRepository.findById(id).orElseThrow(()-> new NotFoundException("media not found"));
    }
    public void deleteById(UUID id){
        Media media = findById(id);
        String resourceType;
        if (media instanceof Video) {
            resourceType = "video";
        } else {
            resourceType = "image";
        }
        try {
            this.cloudinaryConfig.cloudinary().uploader()
                    .destroy(media.getPublicId(),
                            ObjectUtils.asMap(
                            "resource_type", resourceType
                    ));
        } catch (Exception e) {
            throw new BadRequestException("Error deleting media from Cloudinary");
        }
        this.mediaRepository.deleteById(id);
    }
   public Page<Media> findAllMediaByIdAndType(UUID id,String type, int page, int size ){
        Pageable pageable = PageRequest.of(page, size);
        if(type == null ) return  this.mediaRepository.findBySpotId(id,pageable);
        switch (type.toLowerCase()) {
            case "image":
                return this.mediaRepository.findBySpotIdAndFormat(id,type,pageable);
            case "video":
                return this.mediaRepository.findBySpotIdAndFormat(id,type, pageable);
            default:
                return this.mediaRepository.findBySpotId(id,pageable);
        }
    }

}
