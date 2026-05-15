package fra.skatemap.runners;

import fra.skatemap.payloads.RolesDTO;
import fra.skatemap.services.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(1)
public class RoleRunner implements CommandLineRunner {
    private final RoleService roleService;

    public RoleRunner(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) throws Exception {

        RolesDTO admin = new RolesDTO("admin");
        RolesDTO user = new RolesDTO("user");
        if(this.roleService.existByName(admin.roleName()) && this.roleService.existByName(user.roleName())){
            System.out.println("already in the db");
        }
        else {
            this.roleService.save(admin);
            this.roleService.save(user);
        }
    }
}
