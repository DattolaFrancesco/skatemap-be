package fra.skatemap.chatBot.DTO;

import java.util.List;

public record SpotSearchParamsDTO(
        String name,
        String city,
        String country,
        String continents,
        String risk,
        List<String> type,
        String error
) {
}
