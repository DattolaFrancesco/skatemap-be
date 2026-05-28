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
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private byte[] resizeImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(1280, 720)
                .outputQuality(0.80)
                .outputFormat("jpg")
                .toOutputStream(out);
        return out.toByteArray();
    }

    private File compressVideo(MultipartFile file) throws Exception {
        File tempInput = File.createTempFile("input_", ".mp4");
        File tempOutput = File.createTempFile("output_", ".mp4");

        file.transferTo(tempInput);

        VideoAttributes video = new VideoAttributes();
        video.setCodec("libx264");
        video.setBitRate(1500000);

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");
        audio.setBitRate(128000);
        audio.setSamplingRate(44100);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setVideoAttributes(video);
        attrs.setAudioAttributes(audio);
        attrs.setOutputFormat("mp4");

        new Encoder().encode(new MultimediaObject(tempInput), tempOutput, attrs);
        tempInput.delete();

        return tempOutput;
    }

    private CloudinaryUploadResultImageDTO uploadImage(MultipartFile file) {
        try {
            byte[] resized = resizeImage(file);
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    resized,
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
        }
    }

    private CloudinaryUploadResultVideoDTO uploadVideo(MultipartFile file) {
        File tempOutput = null;
        try {
            tempOutput = compressVideo(file);
            Map uploadResult = this.cloudinaryConfig.cloudinary().uploader().upload(
                    tempOutput,
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
        } catch (Exception e) {
            throw new BadRequestException("Error uploading the video");
        } finally {
            if (tempOutput != null) tempOutput.delete();
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
                String mimeType = tika.detect(file.getBytes());
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
        if (this.mediaRepository.countBySpotAndFormat(spot, "video") > 3 || files.size() > 3 ||
                this.mediaRepository.countBySpotAndFormat(spot, "video") + files.size() > 3)
            throw new BadRequestException("you can only upload 3 videos");
        Tika tika = new Tika();
        for (MultipartFile file : files) {
            try {
                String mimeType = tika.detect(file.getBytes());
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