package fra.skatemap.controllers;

import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.RolesDTO;
import fra.skatemap.services.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    @ResponseStatus(HttpStatus.CREATED)
    public RolesDTO save(@RequestBody @Validated RolesDTO rolesDTO, BindingResult validation) {
        if (validation.hasErrors()) throw new BadRequestException("role is not valid");
        return this.roleService.save(rolesDTO);
    }

    @GetMapping
    public List<RolesDTO> findAll() {
        return this.roleService.findAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void DeleteById(@PathVariable UUID id) {
        this.roleService.deleteById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public RolesDTO modifyById(@RequestBody @Validated RolesDTO rolesDTO, BindingResult validation, @PathVariable UUID id) {
        if (validation.hasErrors()) throw new BadRequestException("role is not valid");
        return this.roleService.modifyById(rolesDTO, id);
    }
}
