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
    private final ObjectMapper objectMapper; //convert string  json in java class

    public ChatController(ChatClient.Builder builder, SpotRepository spotRepository, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.spotRepository = spotRepository;
        this.objectMapper = objectMapper;
    }
    private String extractParams( String message){
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
                    - All string values must be plain JSON strings with double quotes: "country": "Italy" not "country": "\\"Italy\\""
                    - For unmentioned fields use null
    
                    Allowed values:
                    - continents: AFRICA, ASIA, EUROPE, NORTHAMERICA, SUDAMERICA, ANTARTIDE, OCEANIA
                    - risk: HIGH, MEDIUM, LOW (always uppercase)
                    - type: JSON array, always uppercase, e.g. ["LEDGE", "RAIL"]. Allowed: RAIL, LEDGE, STAIR, SKATEPARK, STREET
    
                    Validity rules:
                    - A question is valid if it contains AT LEAST ONE of: city, country, continent, type, risk
                    - If valid, extract and leave unmentioned fields null
                    - If the user mentions a sub-region like north/south/east/west of a country set error: "sub_region"
                    - If too vague (none of the above) set error: "too_vague"
                    - If not about skate spots and not about weather set error: "out_of_scope"
                    - If asking about best place for skating based on current weather set error: "weather_search"
                """;
        String raw = chatClient.prompt()
                .user(message)
                .system(instruction)
                .call()
                .content();
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");
        if (start == -1 || end == -1) return "{}";
        return raw.substring(start, end + 1);
    }
    @PostMapping("/ask")
    public Flux<String> chatResponse(@RequestBody String message){
       String parameters =  extractParams(message);
        SpotSearchParamsDTO params = null;
        System.out.println("PARAMS: " + parameters);
        try {params = this.objectMapper.readValue(parameters, SpotSearchParamsDTO.class);}
        catch (JsonProcessingException e) {return Flux.just("Something went wrong, try again.");}
        if(params.error() != null){
            switch (params.error()) {
                case "too_vague" -> {
                    return Flux.just("The message is to vague, try to specific something like country, city etc");
                }
                case "out_of_scope" -> {
                    return Flux.just("I can only respond to skate related question");
                }
                case "weather_search" -> {
                    return Flux.just("For weather advice or info check the spot details on the grid/map visualization");
                }
                case "sub_region" -> {
                    return Flux.just("I can't filter by north/south/east/west, try searching by city or country instead.");
                }
                default -> {
                    return Flux.just("I have internal errors");
                }
            }
        }
        System.out.println("TYPE: " + params.type());
        Specification<Spot> spec = Specification.where(SpotSpecification.build(params));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Spot> result = this.spotRepository.findAll(spec,pageable);
        List<Spot> spots = result.getContent();
        List<SpotBotDTO> spotsDto = spots.stream().map(s->
                new SpotBotDTO(s.getName(),s.getDescription(),s.getContinents().toString(),s.getCountry(),
                        s.getCity(),s.getStreet(),s.getRisk(),
                        s.getSpotTypes().stream().map(t->t.getType().getSpotType())
                                .collect(Collectors.toList()))).toList();
        long numberOfSpots = result.getTotalElements();
        String responseAdded = "";
        if(numberOfSpots>10) responseAdded = "There are more spots like this, try to have a look in the grid/map sections";
        String spotsJson = "";
        try {spotsJson = objectMapper.writeValueAsString(spotsDto);} catch (JsonProcessingException e) {
            return Flux.just("Something went wrong, try again.");}
        String answerInstruction = """
                    You are a skate assistant. The user asked: "%s"
                    Matching spots from the database: %s
    
                    Rules:
                    - Answer ONLY based on the provided spots, never invent spots
                    - If the user asks about weather or skating conditions in a place, search the web for current weather
                    - If the list is empty, say no spots were found
                    - Max 250 tokens
    
                    Formatting:
                    - Plain text only, no markdown, no bold, no bullet points
                    - Each spot in a natural casual sentence mentioning name, city, street, types and risk
                    - Friendly skate culture tone
                """.formatted(message, spotsJson);

        return chatClient.prompt()
                .system(answerInstruction)
                .stream()
                .content()
                .concatWith(Flux.just(responseAdded));
    }

}
