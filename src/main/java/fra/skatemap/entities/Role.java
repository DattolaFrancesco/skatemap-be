package fra.skatemap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "role_name", nullable = false)
    private String roleName;

    public Role(String roleName) {
        this.roleName = roleName;
    }
}
