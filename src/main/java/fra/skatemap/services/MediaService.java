package fra.skatemap.services;

import fra.skatemap.entities.Image;
import fra.skatemap.entities.Media;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.Video;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.repositories.MediaRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final StorageService storageService;

    public MediaService(MediaRepository mediaRepository, StorageService storageService) {
        this.mediaRepository = mediaRepository;
        this.storageService = storageService;
    }

    private byte[] resizeImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(1280, 720)
                .outputQuality(0.80)
                .outputFormat("jpg")
                .toOutputStream(out);
        return out.toByteArray();
    }


    private String uploadImage(MultipartFile file) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(1280, 720)
                    .outputQuality(0.80)
                    .outputFormat("jpg")
                    .toOutputStream(out);
            byte[] resized = out.toByteArray();
            out.reset();
            String key = this.storageService.uploadImage(
                    new ByteArrayInputStream(resized),
                    file.getOriginalFilename(),
                    resized.length);
            return this.storageService.getPublicUrl(key);
        } catch (IOException e) {
            throw new BadRequestException("Error uploading the image");
        }
    }

    @Transactional
    public void saveImage(Spot spot, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        if (this.mediaRepository.countBySpotAndFormat(spot, "image") > 5 || files.size() > 5 ||
                this.mediaRepository.countBySpotAndFormat(spot, "image") + files.size() > 5)
            throw new BadRequestException("you can only upload 5 images");
        for (MultipartFile file : files) {
            if (file.getSize() > 15 * 1024 * 1024)
                throw new BadRequestException("Image too large, max 15MB");
            validateMimeType(file, "image");
            String url = uploadImage(file);
            this.mediaRepository.save(new Image(spot, url, extractKeyFromUrl(url)));
        }
    }
    public void validateMimeType(MultipartFile file, String expectedType) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(expectedType)) {
            throw new BadRequestException("Invalid file type");
        }
    }
    private String extractKeyFromUrl(String url) {
        return url.substring(url.indexOf(".r2.dev/") + ".r2.dev/".length());
    }

   @Transactional
    public void saveVideo(Spot spot, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        if (this.mediaRepository.countBySpotAndFormat(spot, "video") > 1 || files.size() > 1 ||
                this.mediaRepository.countBySpotAndFormat(spot, "video") + files.size() > 1)
            throw new BadRequestException("you can only upload 1 videos");
       for (MultipartFile file : files) {
           try{
           validateMimeType(file, "video");
           String key = this.storageService.uploadRawVideo(
                   file.getInputStream(),
                   file.getOriginalFilename(),
                   file.getSize());
           String url = this.storageService.getRawUrl(key);
           this.mediaRepository.save((new Video(spot,url,key,null)));
           }
           catch (IOException e) {
               throw new BadRequestException(e.getMessage());
           }
       }
    }

    public Media findById(UUID id) {
        return this.mediaRepository.findById(id).orElseThrow(() -> new NotFoundException("media not found"));
    }

    public void deleteById(UUID id) {
        Media media = findById(id);
        try {
            this.storageService.delete(
                    media instanceof Video ? "post-raw" : "post-processed",
                    media.getPublicId()
            );
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        this.mediaRepository.deleteById(id);
    }

    public Page<Media> findAllMediaByIdAndType(UUID id, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (type == null) return this.mediaRepository.findBySpotId(id, pageable);
        return switch (type.toLowerCase()) {
            case "image", "video" -> this.mediaRepository.findBySpotIdAndFormat(id, type, pageable);
            default -> this.mediaRepository.findBySpotId(id, pageable);
        };
    }
}