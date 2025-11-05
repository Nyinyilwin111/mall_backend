package com.spring.Config;

import com.spring.Entity.*;
import com.spring.RepositoryMain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Override
    @Transactional
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
        Branch branch1 = createBranchIfNotFound("Downtown Mall", "123 Main Street, Yangon", "+95-1-1234567");
        Branch branch2 = createBranchIfNotFound("Uptown Mall", "456 Central Road, Mandalay", "+95-2-2345678");
        Branch branch3 = createBranchIfNotFound("Airport Mall", "789 Airport Road, Naypyidaw", "+95-3-3456789");

        // --- USERS ---
        User ceo = createUserIfNotFound("ceo", "ceo@mall.com", "ceo123", ceoRole);
        User manager = createUserIfNotFound("manager", "manager@mall.com", "manager123", managerRole);
        User staff = createUserIfNotFound("staff", "staff@mall.com", "staff123", staffRole);

        // --- USER-BRANCH ASSIGNMENTS ---
        assignUserToBranch(ceo, branch1, "CEO");
        assignUserToBranch(manager, branch2, "MANAGER");
        assignUserToBranch(staff, branch2, "STAFF");

        // --- FLOORS AND SPACES ---
        initializeFloorsAndSpaces(branch1, branch2, branch3);

        System.out.println("=== Data Initialization Completed Successfully ===");
    }

    private void initializeFloorsAndSpaces(Branch branch1, Branch branch2, Branch branch3) {
        // --- BRANCH 1: Downtown Mall ---
        createFloorsAndSpacesForBranch(branch1, "DT", Arrays.asList(
                new FloorData("G", Arrays.asList(
                        new SpaceData("Reception Area", 500.0, "{\"ac\": true, \"wifi\": true, \"reception\": true}"),
                        new SpaceData("Information Desk", 200.0, "{\"ac\": true, \"counter\": true}"),
                        new SpaceData("Security Office", 150.0, "{\"ac\": true, \"monitors\": true}")
                )),
                new FloorData("1", Arrays.asList(
                        new SpaceData("Fashion Store A", 800.0, "{\"ac\": true, \"lighting\": true, \"display\": true}"),
                        new SpaceData("Fashion Store B", 750.0, "{\"ac\": true, \"lighting\": true, \"mirrors\": true}"),
                        new SpaceData("Jewelry Store", 400.0, "{\"ac\": true, \"security\": true, \"display\": true}")
                )),
                new FloorData("2", Arrays.asList(
                        new SpaceData("Electronics Store", 1200.0, "{\"ac\": true, \"power\": true, \"display\": true}"),
                        new SpaceData("Mobile Store", 600.0, "{\"ac\": true, \"charging\": true, \"display\": true}"),
                        new SpaceData("Home Appliances", 900.0, "{\"ac\": true, \"power\": true, \"demo\": true}")
                ))
        ));

        // --- BRANCH 2: Uptown Mall ---
        createFloorsAndSpacesForBranch(branch2, "UP", Arrays.asList(
                new FloorData("G", Arrays.asList(
                        new SpaceData("Main Entrance", 300.0, "{\"ac\": true, \"seating\": true}"),
                        new SpaceData("Customer Service", 250.0, "{\"ac\": true, \"counter\": true, \"wifi\": true}")
                )),
                new FloorData("1", Arrays.asList(
                        new SpaceData("Supermarket", 2500.0, "{\"ac\": true, \"shelving\": true, \"checkout\": true}"),
                        new SpaceData("Pharmacy", 300.0, "{\"ac\": true, \"counter\": true, \"storage\": true}")
                ))
        ));

        // --- BRANCH 3: Airport Mall ---
        createFloorsAndSpacesForBranch(branch3, "AP", Arrays.asList(
                new FloorData("G", Arrays.asList(
                        new SpaceData("Airport Lounge", 1000.0, "{\"ac\": true, \"wifi\": true, \"seating\": true, \"charging\": true}"),
                        new SpaceData("Duty Free Shop", 800.0, "{\"ac\": true, \"display\": true, \"security\": true}")
                )),
                new FloorData("1", Arrays.asList(
                        new SpaceData("Business Center", 600.0, "{\"ac\": true, \"wifi\": true, \"printing\": true, \"meeting\": true}"),
                        new SpaceData("Quick Bite Cafe", 300.0, "{\"ac\": true, \"counter\": true, \"seating\": true}")
                ))
        ));
    }

    private void createFloorsAndSpacesForBranch(Branch branch, String prefix, List<FloorData> floorDataList) {
        for (FloorData floorData : floorDataList) {
            // Create floor
            Floor floor = floorRepository.findByLevelAndBranch(floorData.level, branch)
                    .orElseGet(() -> {
                        Floor newFloor = Floor.builder()
                                .level(floorData.level)
                                .branch(branch)
                                .spaces(new ArrayList<>())
                                .build();
                        return floorRepository.save(newFloor);
                    });

            // Create spaces for this floor
            for (SpaceData spaceData : floorData.spaces) {
                String spaceLocation = prefix + "-" + floorData.level + "-" + spaceData.location.replace(" ", "-");

                spaceRepository.findByLocationAndFloor(spaceData.location, floor)
                        .orElseGet(() -> {
                            Space space = new Space();
                            space.setLocation(spaceLocation);
                            space.setSizeSqft(spaceData.size);
                            space.setAmenities(spaceData.amenities);
                            space.setFloor(floor);
                            return spaceRepository.save(space);
                        });
            }
        }
    }

    // Helper classes for floor and space data
    private static class FloorData {
        String level;
        List<SpaceData> spaces;

        FloorData(String level, List<SpaceData> spaces) {
            this.level = level;
            this.spaces = spaces;
        }
    }

    private static class SpaceData {
        String location;
        Double size;
        String amenities;

        SpaceData(String location, Double size, String amenities) {
            this.location = location;
            this.size = size;
            this.amenities = amenities;
        }
    }

    private Permission createPermissionIfNotFound(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name, description)));
    }

    private Role createRoleIfNotFound(String name, String description, HashSet<Permission> permissions) {
        Role role = roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role newRole = new Role(name, description);
                    // createdAt will be set automatically by @PrePersist
                    return newRole;
                });
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private Branch createBranchIfNotFound(String name, String address, String phoneNumber) {
        return branchRepository.findByName(name)
                .orElseGet(() -> {
                    Branch branch = new Branch();
                    branch.setName(name);
                    branch.setAddress(address);
                    branch.setPhoneNumber(phoneNumber);
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