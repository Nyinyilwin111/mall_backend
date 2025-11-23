package com.sein_gar_har.controller;

import com.sein_gar_har.RepositoryMain.PermissionRepository;
import com.sein_gar_har.RepositoryMain.RoleRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.entity.Permission;
import com.sein_gar_har.entity.Role;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.Services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('ROLE_MANAGEMENT', 'USER_MANAGEMENT')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogService auditLogService;

    // Role Management
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        if (roleRepository.existsByName(role.getName())) {
            return ResponseEntity.badRequest().body("Error: Role already exists!");
        }

        Role savedRole = roleRepository.save(role);

        // Pass complete role data to audit log
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("id", savedRole.getId());
        roleData.put("name", savedRole.getName());
        roleData.put("description", savedRole.getDescription());
        roleData.put("permissions", savedRole.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()));

        auditLogService.logCreate(
                "Role",
                savedRole.getId().toString(),
                roleData
        );

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

        Permission savedPermission = permissionRepository.save(permission);

        // Pass complete permission data to audit log
        Map<String, Object> permData = new HashMap<>();
        permData.put("id", savedPermission.getId());
        permData.put("name", savedPermission.getName());
        permData.put("description", savedPermission.getDescription());

        auditLogService.logCreate(
                "Permission",
                savedPermission.getId().toString(),
                permData
        );

        return ResponseEntity.ok("Permission created successfully!");
    }

    // Alternative delete method that removes permission from roles first
    @DeleteMapping("/permissions/{permissionId}")
    public ResponseEntity<?> deletePermission(@PathVariable Long permissionId) {
        try {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Error: Permission not found"));

            // Store complete permission data for audit log
            Map<String, Object> oldData = new HashMap<>();
            oldData.put("id", permission.getId());
            oldData.put("name", permission.getName());
            oldData.put("description", permission.getDescription());

            // Find all roles that have this permission
            List<Role> rolesWithPermission = roleRepository.findAll().stream()
                    .filter(role -> role.getPermissions().contains(permission))
                    .collect(Collectors.toList());

            // Remove permission from all roles
            for (Role role : rolesWithPermission) {
                role.getPermissions().remove(permission);
                roleRepository.save(role);
            }

            // Now delete the permission
            permissionRepository.delete(permission);

            // Audit log for permission deletion
            auditLogService.logDelete(
                    "Permission",
                    permissionId.toString(),
                    oldData
            );

            return ResponseEntity.ok("Permission deleted successfully! Removed from " +
                    rolesWithPermission.size() + " roles.");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: Failed to delete permission");
        }
    }

    @GetMapping("/permissions")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // User Role Assignment
    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable UUID userId, @RequestBody Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));

        // Store old roles for audit log
        Set<String> oldRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);

        // Store new roles for audit log
        Set<String> newRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // Audit log for role assignment
        Map<String, Object> oldData = Map.of(
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "roles", oldRoles
        );
        Map<String, Object> newData = Map.of(
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "roles", newRoles
        );

        auditLogService.logUpdate(
                "User",
                userId.toString(),
                oldData,
                newData
        );

        return ResponseEntity.ok("Roles assigned successfully!");
    }

    // Alternative delete method that removes role from users first
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable Long roleId) {
        try {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));

            // Store complete role data for audit log
            Map<String, Object> oldData = new HashMap<>();
            oldData.put("id", role.getId());
            oldData.put("name", role.getName());
            oldData.put("description", role.getDescription());
            oldData.put("permissions", role.getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet()));

            // Find all users that have this role
            List<User> usersWithRole = userRepository.findAll().stream()
                    .filter(user -> user.getRoles().contains(role))
                    .collect(Collectors.toList());

            // Remove role from all users
            for (User user : usersWithRole) {
                user.getRoles().remove(role);
                userRepository.save(user);
            }

            // Now delete the role
            roleRepository.delete(role);

            // Audit log for role deletion
            auditLogService.logDelete(
                    "Role",
                    roleId.toString(),
                    oldData
            );

            String message = "Role deleted successfully!";
            if (!usersWithRole.isEmpty()) {
                message += " Removed from " + usersWithRole.size() + " users.";
            }

            return ResponseEntity.ok(message);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: Failed to delete role");
        }
    }

    // Role Permission Assignment
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<?> assignPermissionsToRole(@PathVariable Long roleId, @RequestBody Set<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));

        // Store old data for audit log - Include the complete role information
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("id", role.getId());
        oldData.put("name", role.getName());
        oldData.put("description", role.getDescription());
        oldData.put("permissions", role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()));

        Set<Permission> permissions = permissionNames.stream()
                .map(permissionName -> permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new RuntimeException("Error: Permission not found: " + permissionName)))
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        Role updatedRole = roleRepository.save(role);

        // Store new data for audit log - Include the complete role information
        Map<String, Object> newData = new HashMap<>();
        newData.put("id", updatedRole.getId());
        newData.put("name", updatedRole.getName());
        newData.put("description", updatedRole.getDescription());
        newData.put("permissions", updatedRole.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()));

        // Audit log for permission assignment
        auditLogService.logUpdate(
                "Role",
                roleId.toString(),
                oldData,
                newData
        );

        return ResponseEntity.ok("Permissions assigned successfully!");
    }

    // Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}