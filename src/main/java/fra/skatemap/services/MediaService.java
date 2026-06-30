package fra.skatemap.services;

import fra.skatemap.entities.Image;
import fra.skatemap.entities.Media;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.Video;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.YtAllRequest;
import fra.skatemap.repositories.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private final MediaRepository mediaRepository;
    private final StorageService storageService;

    public MediaService(MediaRepository mediaRepository, StorageService storageService) {
        this.mediaRepository = mediaRepository;
        this.storageService = storageService;
    }

    private String uploadImage(MultipartFile file) {
        File temp = null;
        try {
            temp = File.createTempFile("img-", ".tmp");
            file.transferTo(temp);
            String key = this.storageService.uploadImage(temp, file.getOriginalFilename(), file.getContentType());
            return this.storageService.getPublicUrl(key);
        } catch (IOException e) {
            log.error("IOException while writing temp file for image upload", e);
            throw new BadRequestException("Error uploading the image: " + e.getMessage());
        } catch (Exception e) {
            log.error("Storage upload failed for image '{}'", file.getOriginalFilename(), e);
            throw new BadRequestException("Image storage upload failed: " + e.getMessage());
        } finally {
            if (temp != null && !temp.delete()) {
                log.warn("Could not delete temp file: {}", temp.getAbsolutePath());
            }
        }
    }

    @Transactional
    public void saveImage(Spot spot, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        if (this.mediaRepository.countBySpotAndFormat(spot, "image") + files.size() > 5)
            throw new BadRequestException("you can only upload 5 images");
        for (MultipartFile file : files) {
            if (file.getSize() > 3 * 1024 * 1024)
                throw new BadRequestException("Image too large, max 3MB");
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
        int idx = url.indexOf(".r2.dev/");
        if (idx == -1) {
            // url non nel formato atteso: meglio fallire con un messaggio chiaro
            // che con una StringIndexOutOfBoundsException criptica (-1 + 8 = 7,
            // che avrebbe potuto anche non esplodere subito ma tagliare la stringa
            // a caso, salvando una key sbagliata in DB)
            log.error("Unexpected media URL format, cannot extract R2 key: {}", url);
            throw new BadRequestException("Unexpected storage URL format");
        }
        return url.substring(idx + ".r2.dev/".length());
    }

    @Transactional
    public void saveVideo(Spot spot, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        if (this.mediaRepository.countBySpotAndFormat(spot, "video") + files.size() > 3)
            throw new BadRequestException("you can only upload 3 videos");
        for (MultipartFile file : files) {
            File temp = null;
            try {
                validateMimeType(file, "video");
                temp = File.createTempFile("video-", ".mp4");
                file.transferTo(temp);
                String key = this.storageService.uploadRawVideo(temp, file.getOriginalFilename());
                String url = this.storageService.getRawUrl(key);
                this.mediaRepository.save(new Video(spot, url, key, null));
            } catch (IOException e) {
                log.error("IOException while writing temp file for video upload", e);
                throw new BadRequestException("Error uploading the video: " + e.getMessage());
            } catch (Exception e) {
                // stesso discorso delle immagini: cattura anche gli errori S3/R2,
                // non solo IOException, cosi' la transazione fallisce con un
                // messaggio leggibile invece di un rollback silenzioso.
                log.error("Storage upload failed for video '{}'", file.getOriginalFilename(), e);
                throw new BadRequestException("Video storage upload failed: " + e.getMessage());
            } finally {
                if (temp != null && !temp.delete()) {
                    log.warn("Could not delete temp file: {}", temp.getAbsolutePath());
                }
            }
        }
    }

    @Transactional
    public String getVideo(UUID id) {
        Media video = findById(id);
        Video vid = (Video) video;
        return vid.getLink() + " " + vid.getPublicId() + " " + vid.getStatus();
    }

    @Transactional
    public String setYtAllVideo(List<YtAllRequest> body) {
        for (YtAllRequest bodies : body) {
            Media video = findById(UUID.fromString(bodies.id()));
            Video vid = (Video) video;
            String publicId = vid.getPublicId();
            vid.setStatus("DONE");
            vid.setLink(bodies.link());
            vid.setPublicId(null);
            this.mediaRepository.save(vid);
            try {
                this.storageService.delete("post-raw", publicId);
            } catch (Exception e) {
                // qui un fallimento di delete su R2 non deve far rollback-are
                // l'intero giro di video gia' marcati DONE: logghiamo e proseguiamo,
                // il file raw resta orfano su R2 ma il dato in DB e' corretto.
                log.error("Failed to delete raw video '{}' from storage after marking DONE", publicId, e);
            }
        }
        return "video changed";
    }

    public Media findById(UUID id) {
        return this.mediaRepository.findById(id).orElseThrow(() -> new NotFoundException("media not found"));
    }

    public void deleteById(UUID id) {
        Media media = findById(id);
        if (media.getFormat().equals("video")) {
            Video video = (Video) media;
            if (video.getStatus().equals("DONE")) {
                this.mediaRepository.deleteById(id);
                return;
            }
        }
        try {
            this.storageService.delete(
                    media instanceof Video ? "post-raw" : "post-processed",
                    media.getPublicId()
            );
        } catch (Exception e) {
            log.error("Failed to delete media '{}' from storage", media.getPublicId(), e);
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