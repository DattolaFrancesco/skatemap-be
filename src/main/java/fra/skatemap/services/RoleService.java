package fra.skatemap.services;

import fra.skatemap.entities.Role;
import fra.skatemap.exceptions.NotFoundException;
import fra.skatemap.payloads.RolesDTO;
import fra.skatemap.repositories.RolesRepository;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class RoleService {
    private final RolesRepository rolesRepository;

    public RoleService(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    public RolesDTO save(RolesDTO rolesDTO) {
        Role role = new Role(rolesDTO.roleName());
        this.rolesRepository.save(role);
        return rolesDTO;
    }

    public List<RolesDTO> findAll() {
        List<Role> roles = this.rolesRepository.findAll();
        List<RolesDTO> rolesDto = roles.stream().map(r -> new RolesDTO(r.getRoleName())).toList();
        return rolesDto;
    }

    public void deleteById(UUID id) {
        Role found = this.rolesRepository.findById(id).orElseThrow(() -> new NotFoundException("role not found"));
        this.rolesRepository.delete(found);
    }

    public RolesDTO modifyById(RolesDTO rolesDTO, UUID id) {
        Role found = this.rolesRepository.findById(id).orElseThrow(() -> new NotFoundException("role not found"));
        found.setRoleName(rolesDTO.roleName());
        this.rolesRepository.save(found);
        return new RolesDTO(found.getRoleName());
    }

    public Role findByName(String name) {
        return this.rolesRepository.findByRoleName(name).orElseThrow(() -> new NotFoundException("role not found"));
    }

    public boolean existByName(String name) {
        return this.rolesRepository.existsByRoleName(name);
    }
}
