package fra.skatemap.services;

import fra.skatemap.entities.Role;
import fra.skatemap.entities.User;
import fra.skatemap.entities.UserRole;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.UsersDTO;
import fra.skatemap.repositories.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder encoder;
    private final RoleService roleService;
    private final UserRoleService userRoleService;

    public UsersService(UsersRepository usersRepository,
                        PasswordEncoder encoder, RoleService roleService, UserRoleService userRoleService
    ) {
        this.usersRepository = usersRepository;
        this.encoder = encoder;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
    }

    public User findByEmail(String email) {
        return this.usersRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("email user " + email + " not found"));
    }



    @Transactional
    public User findById(UUID userId) {
        return this.usersRepository.findById(userId).orElseThrow(() -> new NotFoundException("id not found"));
    }

    public User save(UsersDTO usersDTO) {
        if (this.usersRepository.existsByEmail(usersDTO.email())) throw new BadRequestException("email already used");
        if (this.usersRepository.existsByUsername(usersDTO.username())) throw new BadRequestException("username already used");
        User user = new User(usersDTO.username(), usersDTO.email()
                , this.encoder.encode(usersDTO.password()), usersDTO.name(), usersDTO.surname());
        Role role = this.roleService.findByName("user");
        UserRole userRole = new UserRole(user, role);
        User userCreated = this.usersRepository.save(user);
        this.userRoleService.save(userRole);
        return userCreated;
    }

    @Transactional
    public void deleteById(UUID id) {
        this.userRoleService.deleteByUserId(id);
        this.usersRepository.deleteById(id);
    }

   public User modifyById(User user, UsersDTO usersDTO) {
        user.setEmail(usersDTO.email());
        user.setName(usersDTO.name());
        user.setSurname(usersDTO.surname());
        user.setPassword(encoder.encode(usersDTO.password()));
        user.setUsername(usersDTO.username());
        return this.usersRepository.save(user);

    }
}
