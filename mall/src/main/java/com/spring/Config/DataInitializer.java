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
//        // Create CEO user
//        createUserIfNotFound("ceo", "ceo@mall.com", "ceo123", ceoRole);
//        createUserIfNotFound("manager", "manager@mall.com", "manager123", managerRole);
//        createUserIfNotFound("staff", "staff@mall.com", "staff123", staffRole);
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

package com.spring.Config;

import com.spring.Entity.*;
import com.spring.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserBranchRepository userBranchRepository;

    @Override
    public void run(String... args) throws Exception {

        // --- PERMISSIONS ---
        Permission userManagement = createPermissionIfNotFound("USER_MANAGEMENT", "Manage users");
        Permission userRead = createPermissionIfNotFound("USER_READ", "Read user information");
        Permission userWrite = createPermissionIfNotFound("USER_WRITE", "Create/update users");
        Permission spaceManagement = createPermissionIfNotFound("SPACE_MANAGEMENT", "Manage spaces");
        Permission bookingManagement = createPermissionIfNotFound("BOOKING_MANAGEMENT", "Manage bookings");
        Permission roleManagement = createPermissionIfNotFound("ROLE_MANAGEMENT", "Manage roles and permissions");

        // --- ROLES ---
        Role ceoRole = createRoleIfNotFound("CEO", "Chief Executive Officer with full access",
                new HashSet<>(Arrays.asList(userRead, userWrite, spaceManagement, bookingManagement, userManagement, roleManagement)));

        Role managerRole = createRoleIfNotFound("MANAGER", "Store Manager",
                new HashSet<>(Arrays.asList(userRead, spaceManagement, bookingManagement)));

        Role staffRole = createRoleIfNotFound("STAFF", "Store Staff",
                new HashSet<>(Arrays.asList(userRead, bookingManagement)));

        // --- BRANCHES ---
        Branch branch1 = createBranchIfNotFound("Downtown Mall", "Yangon");
        Branch branch2 = createBranchIfNotFound("Uptown Mall", "Mandalay");
        Branch branch3 = createBranchIfNotFound("Airport Mall", "Naypyidaw");

        // --- USERS ---
        User ceo = createUserIfNotFound("ceo", "ceo@mall.com", "ceo123", ceoRole);
        User manager = createUserIfNotFound("manager", "manager@mall.com", "manager123", managerRole);
        User staff = createUserIfNotFound("staff", "staff@mall.com", "staff123", staffRole);

        // --- USER-BRANCH ASSIGNMENTS ---
        assignUserToBranch(ceo, branch1, "CEO");
        assignUserToBranch(manager, branch2, "MANAGER");
        assignUserToBranch(staff, branch2, "STAFF");
    }

    private Permission createPermissionIfNotFound(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name, description)));
    }

    private Role createRoleIfNotFound(String name, String description, HashSet<Permission> permissions) {
        Role role = roleRepository.findByName(name)
                .orElseGet(() -> new Role(name, description));
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private Branch createBranchIfNotFound(String name, String location) {
        return branchRepository.findByName(name)
                .orElseGet(() -> {
                    Branch branch = new Branch();
                    branch.setName(name);
                    branch.setAddress(location);
                    return branchRepository.save(branch);
                });
    }

    private User createUserIfNotFound(String username, String email, String password, Role role) {
        return userRepository.findByName(username)
                .orElseGet(() -> {
                    User user = new User(username, passwordEncoder.encode(password), email);
                    user.setRoles(new HashSet<>(List.of(role)));
                    return userRepository.save(user);
                });
    }

    private void assignUserToBranch(User user, Branch branch, String role) {
        boolean exists = userBranchRepository.existsByUserIdAndBranchId(user.getId(), branch.getId());
        if (!exists) {
            UserBranch userBranch = new UserBranch();
            userBranch.setUser(user);
            userBranch.setBranch(branch);
            userBranch.setRole(role);
            userBranchRepository.save(userBranch);
        }
    }
}
