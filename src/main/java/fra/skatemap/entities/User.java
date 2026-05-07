package fra.skatemap.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"accountNonExpired", "accountNonLocked",
        "credentialsNonExpired", "enabled","password","userRoles"})
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = true)
    private String name;
    @Column(nullable = true)
    private String surname;
    @Column(nullable = true)
    private String avatar;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<UserRole> userRoles;

    public User(String username, String email, String password, String name, String surname, String avatar) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.avatar = avatar;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getRoleName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}