package fra.skatemap.chatBot.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/bot")
public class ChatController {
    private final ChatClient chatClient; //repository

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    @GetMapping("/chat")
    public String chat(){
        return chatClient.prompt()
                .user("dimmi uno scherzo")
                .call()
                .content();
    }
    @PostMapping ("/stream")
    public Flux<String> stream(@RequestBody String message){
        String instruction = "You are a skateboarding assistant. " +
                "You only answer questions about skateboarding (spots, tricks, equipment, culture, safety)" +
                " and weather-related questions when the context is about skating conditions " +
                "(e.g. \"is it good weather to skate today?\"). \n" +
                "For any other topic, reply: \"I can only help with skateboarding and skating conditions.\"\n" +
                "Keep answers concise and practical.";
        return chatClient.prompt()
                .user(message)
                .system(instruction)
                .stream()
                .content();
    }
    @PostMapping ("/test")
    public Flux<String> test(@RequestBody String message){
        String instruction = "respond only with a medium  test sentence";
        return chatClient.prompt()
                .user(message)
                .system(instruction)
                .stream()
                .content();
    }
}
