package fra.skatemap.services;

import fra.skatemap.chatBot.ChatBot;
import fra.skatemap.chatBot.DTO.BotResponseDTO;
import fra.skatemap.repositories.ChatBotRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatBotService {
    private final ChatBotRepository chatBotRepository;

    public ChatBotService(ChatBotRepository chatBotRepository) {
        this.chatBotRepository = chatBotRepository;
    }

    public BotResponseDTO switchStatus(){
        ChatBot chatBot = this.chatBotRepository.findByName("skateBot");
        chatBot.setActive(!chatBot.isActive());
        this.chatBotRepository.save(chatBot);
        return new BotResponseDTO(chatBot.isActive());
    }
    public BotResponseDTO getStatus(){
        ChatBot chatBot = this.chatBotRepository.findByName("skateBot");
        return new BotResponseDTO(chatBot.isActive());
    }
}
