package fra.skatemap.chatBot.DTO;

import java.util.List;


public record SpotBotDTO(
        String name,
        String description,
        String continents,
        String country,
        String city,
        String street,
        String risk,
        List<String> spotTypes
) {
}
