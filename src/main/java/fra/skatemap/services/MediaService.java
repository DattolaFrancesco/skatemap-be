package fra.skatemap.services;

import com.cloudinary.utils.ObjectUtils;
import fra.skatemap.config.CloudinaryConfig;
import fra.skatemap.entities.Image;
import fra.skatemap.entities.Media;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.Video;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.CloudinaryUploadResultImageDTO;
import fra.skatemap.payloads.CloudinaryUploadResultVideoDTO;
import fra.skatemap.repositories.MediaRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
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

    private void resizeImage(MultipartFile file, File dest) throws IOException {
        try (FileOutputStream out = new FileOutputStream(dest)) {
            Thumbnails.of(file.getInputStream())
                    .size(1280, 720)
                    .outputQuality(0.80)
                    .outputFormat("jpg")
                    .toOutputStream(out);
        }
    }

    private CloudinaryUploadResultImageDTO uploadImage(MultipartFile file) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "_image.jpg");
            resizeImage(file, tempFile);
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    tempFile,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );
            return new CloudinaryUploadResultImageDTO(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString(),
                    uploadResult.get("resource_type").toString()
            );
        } catch (IOException e) {
            throw new BadRequestException("Error uploading the image");
        } catch (Exception e) {
            throw new BadRequestException("Cloudinary error: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private CloudinaryUploadResultVideoDTO uploadVideo(MultipartFile file) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "_video");
            file.transferTo(tempFile);
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    tempFile,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );
            String videoUrl = uploadResult.get("secure_url").toString();
            return new CloudinaryUploadResultVideoDTO(
                    videoUrl,
                    uploadResult.get("public_id").toString(),
                    uploadResult.get("resource_type").toString(),
                    buildThumbnailUrl(videoUrl)
            );
        } catch (IOException e) {
            throw new BadRequestException("Error uploading the video");
        } catch (Exception e) {
            throw new BadRequestException("Cloudinary error: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private String buildThumbnailUrl(String videoUrl) {
        return videoUrl
                .replace("/video/upload/", "/video/upload/so_0,w_640/")
                .replaceAll("\\.[^.]+$", ".jpg");
    }

    @Transactional
    public void saveImage(Spot spot, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        if (this.mediaRepository.countBySpotAndFormat(spot, "image") > 5 || files.size() > 5 ||
                this.mediaRepository.countBySpotAndFormat(spot, "image") + files.size() > 5)
            throw new BadRequestException("you can only upload 5 images");
        Tika tika = new Tika();
        for (MultipartFile file : files) {
            try {
                String mimeType = tika.detect(file.getInputStream(), file.getOriginalFilename());
                if (!mimeType.startsWith("image")) throw new BadRequestException("Files aren't images");
            } catch (IOException e) {
                throw new BadRequestException("File is corrupted or unreadable");
            }
            CloudinaryUploadResultImageDTO body = uploadImage(file);
            this.mediaRepository.save(new Image(spot, body.url(), body.publicId()));
        }
    }

    @Transactional
    public void saveVideo(Spot spot, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        if (this.mediaRepository.countBySpotAndFormat(spot, "video") > 1 || files.size() > 1 ||
                this.mediaRepository.countBySpotAndFormat(spot, "video") + files.size() > 1)
            throw new BadRequestException("you can only upload 1 video");
        Tika tika = new Tika();
        for (MultipartFile file : files) {
            try {
                String mimeType = tika.detect(file.getInputStream(), file.getOriginalFilename());
                if (!mimeType.startsWith("video")) throw new BadRequestException("Files aren't video");
            } catch (IOException e) {
                throw new BadRequestException("File is corrupted or unreadable");
            }
            CloudinaryUploadResultVideoDTO body = uploadVideo(file);
            this.mediaRepository.save(new Video(spot, body.url(), body.publicId(), body.thumbnailUrl()));
        }
    }

    public Media findById(UUID id) {
        return this.mediaRepository.findById(id).orElseThrow(() -> new NotFoundException("media not found"));
    }

    public void deleteById(UUID id) {
        Media media = findById(id);
        String resourceType = media instanceof Video ? "video" : "image";
        try {
            this.cloudinaryConfig.cloudinary().uploader().destroy(
                    media.getPublicId(),
                    ObjectUtils.asMap("resource_type", resourceType)
            );
        } catch (Exception e) {
            throw new BadRequestException("Error deleting media from Cloudinary");
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