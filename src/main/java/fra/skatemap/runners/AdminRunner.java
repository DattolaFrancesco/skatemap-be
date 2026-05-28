package fra.skatemap.runners;

import fra.skatemap.entities.Role;
import fra.skatemap.entities.User;
import fra.skatemap.entities.UserRole;
import fra.skatemap.repositories.UsersRepository;
import fra.skatemap.services.RoleService;
import fra.skatemap.services.UserRoleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AdminRunner implements CommandLineRunner {
    private final PasswordEncoder encoder;
    private final RoleService roleService;
    private final UsersRepository usersRepository;
    private final UserRoleService userRoleService;
    private final String adminPassword;

    public AdminRunner(PasswordEncoder encoder, RoleService roleService, UsersRepository usersRepository,
                       UserRoleService userRoleService, @Value("${admin.password}") String adminPassword) {
        this.encoder = encoder;
        this.roleService = roleService;
        this.usersRepository = usersRepository;
        this.userRoleService = userRoleService;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
         String password = adminPassword;
            User user = new User("Sdatten", "sdatten@gmail.com"
                    , this.encoder.encode(password), "francesco", "dattola");
            Role role = this.roleService.findByName("super_admin");
            UserRole userRole = new UserRole(user, role);
            if (this.usersRepository.existsByEmail(user.getEmail())) {
                System.out.println("Admin already exists");
                return;
            }
            User userCreated = this.usersRepository.save(user);
            this.userRoleService.save(userRole);
    }
}
