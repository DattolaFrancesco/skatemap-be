package fra.skatemap.runners;

import fra.skatemap.chatBot.ChatBot;
import fra.skatemap.repositories.ChatBotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class ChatBotRunner implements CommandLineRunner {
    private final ChatBotRepository chatBotRepository;

    public ChatBotRunner(ChatBotRepository chatBotRepository) {
        this.chatBotRepository = chatBotRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        ChatBot bot =  new ChatBot(false,"skateBot");
        if(this.chatBotRepository.existsByName(bot.getName())) return;
        this.chatBotRepository.save(bot);

    }
}
