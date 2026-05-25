package fra.skatemap.payloads;

public record CloudinaryUploadResultImageDTO(
        String url,
        String publicId,
        String resourceType
) {
}
