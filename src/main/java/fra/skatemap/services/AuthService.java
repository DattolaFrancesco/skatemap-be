package fra.skatemap.services;

import fra.skatemap.entities.User;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.exceptions.UnauthorizedException;
import fra.skatemap.payloads.LoginDTO;
import fra.skatemap.security.TokenTools;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UsersService usersService;
    private final TokenTools tokenTools;
    private final PasswordEncoder bcrypt;

    public AuthService(UsersService usersService, TokenTools tokenTools, PasswordEncoder bcrypt) {

        this.usersService = usersService;
        this.tokenTools = tokenTools;
        this.bcrypt = bcrypt;
    }

    public String checkCredentialsAndGenerateToken(LoginDTO body) {
        try {
            User found = this.usersService.findByEmail(body.email());
            if (this.bcrypt.matches(body.password(), found.getPassword())) {
                return this.tokenTools.generateToken(found);

            } else {
                throw new UnauthorizedException("Credenziali errate");
            }
        } catch (NotFoundException ex) {
            throw new UnauthorizedException("Credenziali errate");
        }
    }
}
