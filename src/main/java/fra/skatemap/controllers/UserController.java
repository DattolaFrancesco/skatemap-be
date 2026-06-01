package fra.skatemap.controllers;

import fra.skatemap.entities.User;
import fra.skatemap.entities.UserRole;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.InfoUserDTO;
import fra.skatemap.payloads.RolesDTO;
import fra.skatemap.payloads.UsersDTO;
import fra.skatemap.services.UserRoleService;
import fra.skatemap.services.UsersService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/account")
public class UserController {
    private final UsersService usersService;
    private final UserRoleService userRoleService;

    public UserController(UsersService usersService, UserRoleService userRoleService) {
        this.usersService = usersService;
        this.userRoleService = userRoleService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('super_admin')")
    public Page<UserRole> findAll(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "500") int size,
                                  @RequestParam(defaultValue = "username") String sortBy) {
        return this.userRoleService.findAll(page, size, sortBy);

    }
    @GetMapping("/all/users")
    @PreAuthorize("hasAuthority('super_admin')")
    public Page<UserRole> findAllUsers(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "500") int size,
                                  @RequestParam(defaultValue = "user.name") String sortBy) {
        return this.userRoleService.findAllUsers(page, size, sortBy);

    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('super_admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable UUID id){
        this.usersService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@AuthenticationPrincipal User user){
        this.usersService.deleteById(user.getId());
    }

    @PutMapping
    public User modifyById(@AuthenticationPrincipal User user, @RequestBody @Validated UsersDTO body, BindingResult validation){
        if (validation.hasErrors()) {
            String errors = validation.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            throw new BadRequestException("invalid data: " + errors);
        }
        return this.usersService.modifyById(user,body);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('super_admin')")
    public String modifyById(@PathVariable UUID id, @RequestBody RolesDTO role){
        return this.userRoleService.modifyRoleByUserId(id,role);
    }

    @GetMapping()
    public User getUser(@AuthenticationPrincipal User user) {
        return user;
    }
    @GetMapping("/minimal")
    public InfoUserDTO getUserMinimal(@AuthenticationPrincipal User user) {
        return  this.usersService.getUserMinimal(user);
    }

}
