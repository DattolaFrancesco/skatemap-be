package fra.skatemap.services;

import fra.skatemap.entities.Role;
import fra.skatemap.entities.User;
import fra.skatemap.entities.UserRole;
import fra.skatemap.payloads.RolesDTO;
import fra.skatemap.repositories.UserRolesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserRoleService {
    private final UserRolesRepository userRolesRepository;
    private final RoleService roleService;

    public UserRoleService(UserRolesRepository userRolesRepository, RoleService roleService) {
        this.userRolesRepository = userRolesRepository;
        this.roleService = roleService;
    }

    public UserRole save(UserRole userRole) {
        return this.userRolesRepository.save(userRole);
    }

    public Page<UserRole> findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size);
        return this.userRolesRepository.findAll(pageable);
    }
    public Page<UserRole> findAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return this.userRolesRepository.findByRoleRoleNameIn(List.of("user","admin"),pageable);
    }
    public void deleteByUserId(UUID id){
        this.userRolesRepository.deleteByUserId(id);
    }
    public UserRole findByUserId(UUID id){
        return  this.userRolesRepository.findByUserId(id);
    }
    public String modifyRoleByUserId(UUID id, RolesDTO newRole){
        UserRole found = this.userRolesRepository.findByUserId(id);
        Role roleFound = this.roleService.findByName(newRole.roleName());
        found.setRole(roleFound);
        this.userRolesRepository.save(found);
        return newRole.roleName()+" is the new role for this user";
    }
}
