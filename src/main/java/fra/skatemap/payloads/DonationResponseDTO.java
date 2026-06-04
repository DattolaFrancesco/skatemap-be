package fra.skatemap.payloads;

public record DonationResponseDTO(
        String clientSecret,
        String publishableKey
) {
}
