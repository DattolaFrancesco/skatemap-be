package fra.skatemap.services;

import fra.skatemap.entities.UserRole;
import fra.skatemap.repositories.UserRolesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserRoleService {
    private final UserRolesRepository userRolesRepository;

    public UserRoleService(UserRolesRepository userRolesRepository) {
        this.userRolesRepository = userRolesRepository;
    }

    public UserRole save(UserRole userRole) {
        return this.userRolesRepository.save(userRole);
    }

    public Page<UserRole> findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size);
        return this.userRolesRepository.findAll(pageable);
    }
    public Page<UserRole> findAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size);
        return this.userRolesRepository.findByRoleRoleNameIn(List.of("user","admin"),pageable);
    }
    public void deleteByUserId(UUID id){
        this.userRolesRepository.deleteByUserId(id);
    }
    public UserRole findByUserId(UUID id){
        return  this.userRolesRepository.findByUserId(id);
    }
}
