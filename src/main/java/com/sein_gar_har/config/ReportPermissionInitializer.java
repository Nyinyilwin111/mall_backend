//package com.sein_gar_har.config;
//
//import com.sein_gar_har.RepositoryMain.PermissionRepository;
//import com.sein_gar_har.RepositoryMain.RoleRepository;
//import com.sein_gar_har.entity.Permission;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//
//@Component
//@Order(2)
//public class ReportPermissionInitializer implements CommandLineRunner {
//
//    @Autowired
//    private PermissionRepository permissionRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        initializeReportPermissions();
//    }
//
//    private void initializeReportPermissions() {
//        // Create report permissions
//        Permission reportRead = createPermissionIfNotFound("REPORT_READ", "Can view and generate reports");
//        Permission reportWrite = createPermissionIfNotFound("REPORT_WRITE", "Can create and manage reports");
//        Permission reportExport = createPermissionIfNotFound("REPORT_EXPORT", "Can export reports to various formats");
//
//        // Assign permissions to roles
//        assignReportPermissionsToRoles(reportRead, reportWrite, reportExport);
//    }
//
//    private Permission createPermissionIfNotFound(String name, String description) {
//        return permissionRepository.findByName(name)
//                .orElseGet(() -> permissionRepository.save(new Permission(name, description)));
//    }
//
//    private void assignReportPermissionsToRoles(Permission reportRead, Permission reportWrite, Permission reportExport) {
//        // Assign to CEO
//        roleRepository.findByName("CEO").ifPresent(ceoRole -> {
//            ceoRole.getPermissions().addAll(Arrays.asList(reportRead, reportWrite, reportExport));
//            roleRepository.save(ceoRole);
//        });
//
//        // Assign to MANAGER
//        roleRepository.findByName("MANAGER").ifPresent(managerRole -> {
//            managerRole.getPermissions().addAll(Arrays.asList(reportRead, reportWrite));
//            roleRepository.save(managerRole);
//        });
//
//        // Assign to ADMIN (if exists)
//        roleRepository.findByName("ADMIN").ifPresent(adminRole -> {
//            adminRole.getPermissions().addAll(Arrays.asList(reportRead, reportWrite, reportExport));
//            roleRepository.save(adminRole);
//        });
//
//        // Assign to STAFF (read only)
//        roleRepository.findByName("STAFF").ifPresent(staffRole -> {
//            staffRole.getPermissions().add(reportRead);
//            roleRepository.save(staffRole);
//        });
//    }
//}