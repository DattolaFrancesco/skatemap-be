package fra.skatemap.payloads;

public record CloudinaryUploadResultDTO(
        String url,
        String publicId,
        String resourceType
) {
}
