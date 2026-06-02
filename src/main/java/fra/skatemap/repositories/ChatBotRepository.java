package fra.skatemap.repositories;

import fra.skatemap.chatBot.ChatBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatBotRepository extends JpaRepository<ChatBot, UUID> {
    boolean existsByName(String name);
    ChatBot findByName(String name);
}
