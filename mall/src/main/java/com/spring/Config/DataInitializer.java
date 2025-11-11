//package com.spring.Config;
//
//import com.spring.Entity.Permission;
//import com.spring.Entity.Role;
//import com.spring.Entity.User;
//import com.spring.Repository.PermissionRepository;
//import com.spring.Repository.RoleRepository;
//import com.spring.Repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.HashSet;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private PermissionRepository permissionRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // Create permissions
//        Permission userManagement = createPermissionIfNotFound("USER_MANAGEMENT", "Manage users");
//        Permission userRead = createPermissionIfNotFound("USER_READ", "Read user information");
//        Permission userWrite = createPermissionIfNotFound("USER_WRITE", "Create/update users");
//        Permission spaceManagement = createPermissionIfNotFound("SPACE_MANAGEMENT", "Manage spaces");
//        Permission bookingManagement = createPermissionIfNotFound("BOOKING_MANAGEMENT", "Manage booking");
//        Permission roleManagement = createPermissionIfNotFound("ROLE_MANAGEMENT", "Manage roles and permissions");
//        Permission SpaceRead = createPermissionIfNotFound("SPACE_READ", "Read space information");
//
//        // Create roles
//        Role ceoRole = createRoleIfNotFound("CEO", "Chief Executive Officer with full access",
//                new HashSet<>(Arrays.asList(userRead, userWrite, spaceManagement, bookingManagement,userManagement, roleManagement)));
//
//        Role managerRole = createRoleIfNotFound("MANAGER", "Store Manager",
//                new HashSet<>(Arrays.asList(userRead, spaceManagement, bookingManagement)));
//
//        Role staffRole = createRoleIfNotFound("STAFF", "Store Staff",
//                new HashSet<>(Arrays.asList(userRead, bookingManagement)));
//
//        Role GuestRole = createRoleIfNotFound("GUEST", "Guest",
//                new HashSet<>(Arrays.asList(SpaceRead,userWrite)));
//
//        // Create CEO user
//        createUserIfNotFound("ceo", "ceo@mall.com", "ceo123", ceoRole);
//        createUserIfNotFound("manager", "manager@mall.com", "manager123", managerRole);
//        createUserIfNotFound("staff", "staff@mall.com", "staff123", staffRole);
//        createUserIfNotFound("guest", "guest@gmail.com", "guest123", GuestRole);
//    }
//
//    private Permission createPermissionIfNotFound(String name, String description) {
//        return permissionRepository.findByName(name)
//                .orElseGet(() -> permissionRepository.save(new Permission(name, description)));
//    }
//
//    private Role createRoleIfNotFound(String name, String description, HashSet<Permission> permissions) {
//        Role role = roleRepository.findByName(name)
//                .orElseGet(() -> new Role(name, description));
//        role.setPermissions(permissions);
//        return roleRepository.save(role);
//    }
//
//    private void createUserIfNotFound(String username, String email, String password, Role role) {
//        if (!userRepository.existsByFullName(username)) {
//            User user = new User(username, passwordEncoder.encode(password), email);
//            user.setRoles(new HashSet<>(Arrays.asList(role)));
//            userRepository.save(user);
//        }
//    }
//}