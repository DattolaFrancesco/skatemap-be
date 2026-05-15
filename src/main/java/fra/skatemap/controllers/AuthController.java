package fra.skatemap.controllers;

import fra.skatemap.entities.User;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.LoginDTO;
import fra.skatemap.payloads.TokenResponseDTO;
import fra.skatemap.payloads.UsersDTO;
import fra.skatemap.payloads.UsersResponseDTO;
import fra.skatemap.services.AuthService;
import fra.skatemap.services.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UsersService usersService;

    public AuthController(AuthService authService, UsersService usersService) {
        this.authService = authService;
        this.usersService = usersService;
    }

    // POST /auth/register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsersResponseDTO register(@RequestBody @Validated UsersDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            String errors = validation.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining("\n- ", "- ", ""));
            throw new BadRequestException(errors);
        }
        User user = this.usersService.save(body);
        return new UsersResponseDTO(user.getId());
    }

    // POST /auth/login
    @PostMapping("/login")
    public TokenResponseDTO login(@RequestBody @Validated LoginDTO body, BindingResult validation) {
        if (validation.hasErrors()) throw new BadRequestException("Credenziali non valide");
        return new TokenResponseDTO(this.authService.checkCredentialsAndGenerateToken(body), LocalDate.now());
    }
}