package fra.skatemap.payloads;

public record CloudinaryUploadResultVideoDTO(
        String url,
        String publicId,
        String resourceType,
        String thumbnailUrl
) {
}
