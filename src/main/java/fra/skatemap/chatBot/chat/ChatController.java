package fra.skatemap.chatBot.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fra.skatemap.chatBot.DTO.SpotBotDTO;
import fra.skatemap.chatBot.DTO.SpotSearchParamsDTO;
import fra.skatemap.config.SpotSpecification;
import fra.skatemap.entities.Spot;
import fra.skatemap.repositories.SpotRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bot")
public class ChatController {
    private final ChatClient chatClient;
    private final SpotRepository spotRepository;
    private final ObjectMapper objectMapper;

    public ChatController(ChatClient.Builder builder, SpotRepository spotRepository, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.spotRepository = spotRepository;
        this.objectMapper = objectMapper;
    }

    private String extractParams(String message) {
        String instruction = """
                You are a parameter extractor for a skate spot database.
                Respond ONLY with a valid JSON object. No markdown, no explanation, no extra text.

                Structure:
                {
                  "name": null,
                  "city": null,
                  "country": null,
                  "continents": null,
                  "risk": null,
                  "type": null,
                  "error": null
                }

                Extraction rules:
                - Always translate location names to English: "Italia" → "Italy", "Milano" → "Milan", "Europa" → "EUROPE"
                - All string values must be plain JSON strings with double quotes
                - For unmentioned fields use null
                - For city: extract ONLY the bare city name, never include prefixes like "Province of", "Metropolitan City of", "Free municipal consortium of". Example: "Metropolitan City of Florence" → "Florence"
                - If message mentions sub-region (north/south/east/west of a country) → error: "sub_region"
                - If message is about skate culture, tricks, skaters, brands (no location at all) → error: "skate_knowledge"
                - In all other cases → error: null

                Allowed values:
                - continents: AFRICA, ASIA, EUROPE, NORTHAMERICA, SUDAMERICA, ANTARTIDE, OCEANIA
                - risk: HIGH, MEDIUM, LOW (always uppercase)
                - type: JSON array, always uppercase, e.g. ["LEDGE", "RAIL"]. Allowed: RAIL, LEDGE, STAIR, SKATEPARK, STREET
                """;
        String raw = chatClient.prompt()
                .user(message)
                .system(instruction)
                .options(OpenAiChatOptions.builder().maxTokens(250).build())
                .call()
                .content();
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");
        if (start == -1 || end == -1) return "{}";
        return raw.substring(start, end + 1);
    }

    private Flux<String> skateKnowledgeResponse(String message) {
        String skateInstruction = """
                You are a knowledgeable skate assistant. Answer the following question about skateboarding: "%s"
                Rules:
                - Answer only about skateboarding topics (tricks, brands, skaters, culture, history)
                - Max 250 tokens
                - Plain text only, no markdown, no bold, no bullet points
                - Friendly skate culture tone
                """.formatted(message);
        return chatClient.prompt()
                .system(skateInstruction)
                .options(OpenAiChatOptions.builder().maxTokens(250).build())
                .stream()
                .content();
    }

    @PostMapping("/ask")
    public Flux<String> chatResponse(@RequestBody String message) {
        String parameters = extractParams(message);
        System.out.println("PARAMS: " + parameters);

        SpotSearchParamsDTO params;
        try { params = this.objectMapper.readValue(parameters, SpotSearchParamsDTO.class); }
        catch (JsonProcessingException e) { return Flux.just("Something went wrong, try again."); }

        if (params.error() != null) {
            boolean hasParams = params.city() != null || params.country() != null ||
                    params.continents() != null || params.type() != null ||
                    params.risk() != null;

            if (!params.error().equals("skate_knowledge") || !hasParams) {
                return switch (params.error()) {
                    case "sub_region" -> Flux.just("I can't filter by north/south/east/west, try searching by city or country instead.");
                    case "skate_knowledge" -> skateKnowledgeResponse(message);
                    default -> Flux.just("I have internal errors");
                };
            }
        }

        boolean hasParams = params.city() != null || params.country() != null ||
                params.continents() != null || params.type() != null ||
                params.risk() != null;

        if (!hasParams) {
            String classifyInstruction = """
                    Is this message about skateboarding (tricks, brands, skaters, culture, history)? Answer only "yes" or "no".
                    Message: "%s"
                    """.formatted(message);
            String answer = chatClient.prompt()
                    .system(classifyInstruction)
                    .options(OpenAiChatOptions.builder().maxTokens(10).build())
                    .call()
                    .content()
                    .trim()
                    .toLowerCase();
            if (answer.contains("yes")) {
                return skateKnowledgeResponse(message);
            } else {
                return Flux.just("The message is too vague, try to specify something like country, city etc");
            }
        }

        System.out.println("TYPE: " + params.type());
        Specification<Spot> spec = Specification.where(SpotSpecification.build(params));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Spot> result = this.spotRepository.findAll(spec, pageable);
        List<Spot> spots = result.getContent();
        List<SpotBotDTO> spotsDto = spots.stream().map(s ->
                new SpotBotDTO(s.getName(), s.getDescription(), s.getContinents().toString(), s.getCountry(),
                        s.getCity(), s.getStreet(), s.getRisk(),
                        s.getSpotTypes().stream().map(t -> t.getType().getSpotType())
                                .collect(Collectors.toList()))).toList();

        long numberOfSpots = result.getTotalElements();
        String responseAdded = numberOfSpots > 10 ? "There are more spots like this, try to have a look in the grid/map sections" : "";

        String spotsJson;
        try { spotsJson = objectMapper.writeValueAsString(spotsDto); }
        catch (JsonProcessingException e) { return Flux.just("Something went wrong, try again."); }

        String answerInstruction = """
                You are a skate assistant. The user asked: "%s"
                Matching spots from the database: %s

                Rules:
                - Answer ONLY based on the provided spots, never invent spots
                - If the list is empty, say no spots were found
                - Max 250 tokens

                Formatting:
                - Plain text only, no markdown, no bold, no bullet points
                - Each spot in a natural casual sentence mentioning name, city, street, types and risk
                - Friendly skate culture tone
                """.formatted(message, spotsJson);

        return chatClient.prompt()
                .system(answerInstruction)
                .options(OpenAiChatOptions.builder().maxTokens(250).build())
                .stream()
                .content()
                .concatWith(Flux.just(responseAdded));
    }
}