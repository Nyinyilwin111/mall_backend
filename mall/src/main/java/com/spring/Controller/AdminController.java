package com.spring.Controller;

import com.spring.Entity.Permission;
import com.spring.Entity.Role;
import com.spring.Entity.User;
import com.spring.Repository.PermissionRepository;
import com.spring.Repository.RoleRepository;
import com.spring.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ROLE_MANAGEMENT')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    // Role Management
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        if (roleRepository.existsByName(role.getName())) {
            return ResponseEntity.badRequest().body("Error: Role already exists!");
        }
        roleRepository.save(role);
        return ResponseEntity.ok("Role created successfully!");
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Permission Management
    @PostMapping("/permissions")
    public ResponseEntity<?> createPermission(@RequestBody Permission permission) {
        if (permissionRepository.existsByName(permission.getName())) {
            return ResponseEntity.badRequest().body("Error: Permission already exists!");
        }
        permissionRepository.save(permission);
        return ResponseEntity.ok("Permission created successfully!");
    }

    @GetMapping("/permissions")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // User Role Assignment
    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @RequestBody Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("Roles assigned successfully!");
    }

    // Role Permission Assignment
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<?> assignPermissionsToRole(@PathVariable Long roleId, @RequestBody Set<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));

        Set<Permission> permissions = permissionNames.stream()
                .map(permissionName -> permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new RuntimeException("Error: Permission not found: " + permissionName)))
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        roleRepository.save(role);

        return ResponseEntity.ok("Permissions assigned successfully!");
    }

    // Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}