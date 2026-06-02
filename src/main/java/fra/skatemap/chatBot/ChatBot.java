package fra.skatemap.chatBot;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "chat_bot")
@NoArgsConstructor
public class ChatBot {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public ChatBot(boolean active, String name){
        this.active = active;
        this.name = name;
    }
}
