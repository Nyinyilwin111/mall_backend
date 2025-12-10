package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.BranchRepository;
import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.BranchService;
import com.sein_gar_har.Services.AuditLogService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.request.*;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.Lease;
import com.sein_gar_har.entity.Payment;
import com.sein_gar_har.entity.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    UserRepository userRepository;

    private final AuditLogService auditLogService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private HttpServletRequest request;

    @Override
    public List<BranchResponseDTO> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponseDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        return convertToResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponseDTO createBranch(CreateBranchRequestDTO requestDTO) {
        // Get current user with multiple fallback strategies
        String currentUser = getCurrentUserWithMultipleStrategies();
        System.out.println("=== CREATE BRANCH - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            if (branchRepository.existsByName(requestDTO.getName())) {
                throw new RuntimeException("Branch name already exists: " + requestDTO.getName());
            }

            Branch branch = new Branch();
            branch.setName(requestDTO.getName());
            branch.setAddress(requestDTO.getAddress());
            branch.setPhoneNumber(requestDTO.getPhoneNumber());

            Branch savedBranch = branchRepository.save(branch);

            // Audit log for branch creation
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", savedBranch.getId());
            newData.put("name", savedBranch.getName());
            newData.put("address", savedBranch.getAddress());
            newData.put("phoneNumber", savedBranch.getPhoneNumber());

            auditLogService.logCreate("Branch", savedBranch.getId().toString(), newData);

            return convertToResponse(savedBranch);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public BranchResponseDTO updateBranch(Long id, UpdateBranchRequestDTO requestDTO) {
        // Get current user with multiple fallback strategies
        String currentUser = getCurrentUserWithMultipleStrategies();
        System.out.println("=== UPDATE BRANCH - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Branch existingBranch = branchRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

            // Store old data for audit log
            Map<String, Object> oldData = new HashMap<>();
            oldData.put("id", existingBranch.getId());
            oldData.put("name", existingBranch.getName());
            oldData.put("address", existingBranch.getAddress());
            oldData.put("phoneNumber", existingBranch.getPhoneNumber());

            if (!existingBranch.getName().equals(requestDTO.getName()) &&
                    branchRepository.existsByName(requestDTO.getName())) {
                throw new RuntimeException("Branch name already exists: " + requestDTO.getName());
            }

            existingBranch.setName(requestDTO.getName());
            existingBranch.setAddress(requestDTO.getAddress());
            existingBranch.setPhoneNumber(requestDTO.getPhoneNumber());

            Branch updatedBranch = branchRepository.save(existingBranch);

            // Store new data for audit log
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", updatedBranch.getId());
            newData.put("name", updatedBranch.getName());
            newData.put("address", updatedBranch.getAddress());
            newData.put("phoneNumber", updatedBranch.getPhoneNumber());

            auditLogService.logUpdate("Branch", id.toString(), oldData, newData);

            return convertToResponse(updatedBranch);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        // Get current user with multiple fallback strategies
        String currentUser = getCurrentUserWithMultipleStrategies();
        System.out.println("=== DELETE BRANCH - Current user: " + currentUser + " ===");

        if (!"System".equals(currentUser)) {
            AuditLogService.setBranchOperationUser(currentUser);
        }

        try {
            Branch branch = branchRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

            // Store data for audit log before deletion
            Map<String, Object> oldData = new HashMap<>();
            oldData.put("id", branch.getId());
            oldData.put("name", branch.getName());
            oldData.put("address", branch.getAddress());
            oldData.put("phoneNumber", branch.getPhoneNumber());

            branchRepository.deleteById(id);

            auditLogService.logDelete("Branch", id.toString(), oldData);
        } finally {
            AuditLogService.clearBranchOperationUser();
        }
    }

    @Override
    public boolean existsByName(String name) {
        return branchRepository.existsByName(name);
    }

    @Override
    public List<BranchResponseDTO> getBranchesForCurrentUser() {
        try {
            String username = getCurrentUsernameWithMultipleStrategies();
            System.out.println("=== getBranchesForCurrentUser - Username: " + username + " ===");

            if (username == null || "System".equals(username) || "anonymousUser".equals(username)) {
                return List.of();
            }

            // Try to find user by email first
            Optional<User> userOptional = userRepository.findByEmail(username);

            if (userOptional.isEmpty()) {
                // Try to find by fullName as fallback
                Optional<User> userByFullName = userRepository.findByFullName(username);
                if (userByFullName.isPresent()) {
                    User user = userByFullName.get();
                    return getUserBranches(user);
                } else {
                    throw new RuntimeException("User not found: " + username);
                }
            }

            User user = userOptional.get();
            return getUserBranches(user);

        } catch (Exception e) {
            System.err.println("Error in getBranchesForCurrentUser: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private List<BranchResponseDTO> getUserBranches(User user) {
        // Check if user has tenant role
        boolean isTenant = user.getRoles().stream()
                .anyMatch(role -> "TENANT".equalsIgnoreCase(role.getName()));

        if (isTenant) {
            // Return only branches assigned to this tenant
            return user.getBranches().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } else {
            // Return all branches for admin/manager users
            return branchRepository.findAll().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
    }

    private BranchResponseDTO convertToResponse(Branch branch) {
        BranchResponseDTO response = new BranchResponseDTO();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setAddress(branch.getAddress());
        response.setPhoneNumber(branch.getPhoneNumber());
        response.setCreatedAt(branch.getCreatedAt());
        response.setUpdatedAt(branch.getUpdatedAt());
        return response;
    }

    /**
     * MULTIPLE STRATEGIES TO GET CURRENT USER
     */
    private String getCurrentUserWithMultipleStrategies() {
        // Strategy 1: Try Security Context first
        String userFromSecurity = getCurrentUserFromSecurityContext();
        if (!"System".equals(userFromSecurity)) {
            return userFromSecurity;
        }

        // Strategy 2: Try JWT Token from Authorization header
        String userFromJwt = getCurrentUserFromJwtToken();
        if (!"System".equals(userFromJwt)) {
            return userFromJwt;
        }

        // Strategy 3: Last resort - check if there's a test header
        String userFromHeader = getCurrentUserFromCustomHeader();
        if (!"System".equals(userFromHeader)) {
            return userFromHeader;
        }

        return "System";
    }

    private String getCurrentUsernameWithMultipleStrategies() {
        // Strategy 1: Try Security Context first
        String usernameFromSecurity = getCurrentUsernameFromSecurityContext();
        if (isValidUsername(usernameFromSecurity)) {
            return usernameFromSecurity;
        }

        // Strategy 2: Try JWT Token from Authorization header
        String usernameFromJwt = getCurrentUsernameFromJwtToken();
        if (isValidUsername(usernameFromJwt)) {
            return usernameFromJwt;
        }

        return null;
    }

    /**
     * Strategy 1: Security Context
     */
    private String getCurrentUserFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("=== SECURITY CONTEXT STRATEGY ===");
            System.out.println("Authentication: " + authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                System.out.println("Principal: " + principal);

                String username = extractUsernameFromPrincipal(principal);
                System.out.println("Extracted username: " + username);

                if (isValidUsername(username)) {
                    Optional<User> user = userRepository.findByEmail(username);
                    if (user.isPresent()) {
                        String fullName = user.get().getFullName();
                        System.out.println("Found user full name: " + fullName);
                        return (fullName != null && !fullName.trim().isEmpty()) ? fullName : username;
                    }
                    return username;
                }
            }

            System.out.println("Security Context: No authenticated user found");
            return "System";

        } catch (Exception e) {
            System.err.println("Error in Security Context strategy: " + e.getMessage());
            return "System";
        }
    }

    private String getCurrentUsernameFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                return extractUsernameFromPrincipal(principal);
            }
        } catch (Exception e) {
            System.err.println("Error getting username from SecurityContext: " + e.getMessage());
        }
        return null;
    }

    /**
     * Strategy 2: JWT Token from Authorization Header
     */
    private String getCurrentUserFromJwtToken() {
        try {
            String token = extractTokenFromRequest();
            if (token != null && !token.trim().isEmpty()) {
                System.out.println("=== JWT TOKEN STRATEGY ===");
                System.out.println("Token found, length: " + token.length());

                Claims claims = tokenProvider.getClaimsFromToken(token);
                String email = claims.get(JwtConstants.EMAIL, String.class);

                System.out.println("Email from JWT: " + email);

                if (email != null && !email.trim().isEmpty()) {
                    Optional<User> user = userRepository.findByEmail(email);
                    if (user.isPresent()) {
                        String fullName = user.get().getFullName();
                        System.out.println("JWT User found: " + fullName + " (" + email + ")");
                        return fullName != null && !fullName.trim().isEmpty() ? fullName : email;
                    } else {
                        System.out.println("JWT User not found for email: " + email);
                        return email;
                    }
                }
            } else {
                System.out.println("JWT Strategy: No token found in request");
            }
        } catch (Exception e) {
            System.err.println("JWT Strategy Error: " + e.getMessage());
        }
        return "System";
    }

    private String getCurrentUsernameFromJwtToken() {
        try {
            String token = extractTokenFromRequest();
            if (token != null && !token.trim().isEmpty()) {
                Claims claims = tokenProvider.getClaimsFromToken(token);
                return claims.get(JwtConstants.EMAIL, String.class);
            }
        } catch (Exception e) {
            System.err.println("Error extracting username from JWT: " + e.getMessage());
        }
        return null;
    }

    /**
     * Strategy 3: Custom Header (for testing)
     */
    private String getCurrentUserFromCustomHeader() {
        try {
            String testUser = request.getHeader("X-Test-User");
            if (testUser != null && !testUser.trim().isEmpty()) {
                System.out.println("=== CUSTOM HEADER STRATEGY ===");
                System.out.println("Found test user from header: " + testUser);
                return testUser;
            }
        } catch (Exception e) {
            System.err.println("Error in custom header strategy: " + e.getMessage());
        }
        return "System";
    }

    /**
     * Helper Methods
     */
    private String extractTokenFromRequest() {
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("Authorization Header: " + (authHeader != null ? authHeader : "Null"));

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                System.out.println("Extracted token length: " + token.length());
                return token;
            } else {
                System.out.println("No Bearer token found in header");
            }
        } catch (Exception e) {
            System.err.println("Error extracting token from request: " + e.getMessage());
        }
        return null;
    }

    private String extractUsernameFromPrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    private boolean isValidUsername(String username) {
        return username != null &&
                !username.trim().isEmpty() &&
                !"anonymousUser".equals(username) &&
                !"system".equalsIgnoreCase(username);
    }

    // Month names for display
    private static final String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

    @Override
    public List<DashboardMetricDTO> getRentalMetrics(Integer branchId) {
        List<DashboardMetricDTO> metrics = new ArrayList<>();

        // Total Revenue (last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);

        BigDecimal currentRevenue = branchRepository.getRevenueInPeriod(thirtyDaysAgo, LocalDate.now(), branchId);
        BigDecimal previousRevenue = branchRepository.getRevenueInPeriod(sixtyDaysAgo, thirtyDaysAgo, branchId);
        BigDecimal revenueChange = calculatePercentageChange(previousRevenue, currentRevenue);

        metrics.add(DashboardMetricDTO.builder()
                .label("Total Revenue")
                .value(currentRevenue.setScale(0, RoundingMode.HALF_UP).doubleValue())
                .currency("£")
                .change(revenueChange.doubleValue())
                .trend(revenueChange.compareTo(BigDecimal.ZERO) >= 0 ? "up" : "down")
                .build());

        // Occupancy Rate
        Long totalSpaces = branchRepository.countTotalSpaces(branchId);
        Long occupiedSpaces = branchRepository.countOccupiedSpaces(branchId);
        double occupancyRate = totalSpaces > 0 ? (occupiedSpaces.doubleValue() / totalSpaces.doubleValue()) * 100 : 0;

        metrics.add(DashboardMetricDTO.builder()
                .label("Occupancy Rate")
                .value((double) Math.round(occupancyRate))
                .currency("%")
                .change(0.0) // You can add change calculation if you have historical data
                .trend("neutral")
                .build());

        // Average Rent
        BigDecimal avgRent = branchRepository.getAverageRent(branchId);
        metrics.add(DashboardMetricDTO.builder()
                .label("Avg. Rent")
                .value(avgRent.setScale(0, RoundingMode.HALF_UP).doubleValue())
                .currency("£")
                .change(0.0)
                .trend("neutral")
                .build());

        // Active Leases
        List<Lease> activeLeases = branchRepository.getActiveLeases(branchId);
        metrics.add(DashboardMetricDTO.builder()
                .label("Active Leases")
                .value((double) activeLeases.size())
                .change(0.0)
                .trend("neutral")
                .build());

        // Total Spaces
        metrics.add(DashboardMetricDTO.builder()
                .label("Total Spaces")
                .value(totalSpaces.doubleValue())
                .change(0.0)
                .trend("neutral")
                .build());

        // Revenue per Month
        metrics.add(DashboardMetricDTO.builder()
                .label("Revenue/Month")
                .value(currentRevenue.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP).doubleValue())
                .currency("£")
                .change(revenueChange.doubleValue())
                .trend(revenueChange.compareTo(BigDecimal.ZERO) >= 0 ? "up" : "down")
                .build());

        return metrics;
    }

    @Override
    public AccountsReceivableDTO getAccountsReceivable(Integer branchId) {
        List<Lease> activeLeases = branchRepository.getActiveLeases(branchId);

        BigDecimal current = BigDecimal.ZERO;
        BigDecimal days1_30 = BigDecimal.ZERO;
        BigDecimal days31_60 = BigDecimal.ZERO;
        BigDecimal days61_90 = BigDecimal.ZERO;
        BigDecimal over90 = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        for (Lease lease : activeLeases) {
            BigDecimal rentAmount = lease.getRentAmount();
            LocalDate startDate = lease.getStartDate();

            // Calculate months since lease started
            int monthsSinceStart = (today.getYear() - startDate.getYear()) * 12 +
                    (today.getMonthValue() - startDate.getMonthValue());

            if (monthsSinceStart <= 0) continue;

            // Get payments for this lease
            List<Payment> leasePayments = branchRepository.getPaymentsByLeaseId(lease.getLeaseId());
            BigDecimal totalPaid = leasePayments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate rent due for completed months
            BigDecimal totalRentDue = rentAmount.multiply(BigDecimal.valueOf(monthsSinceStart));
            BigDecimal rentDue = totalRentDue.subtract(totalPaid);

            if (rentDue.compareTo(BigDecimal.ZERO) > 0) {
                // Calculate due date for each month
                for (int i = 1; i <= monthsSinceStart; i++) {
                    LocalDate dueDate = startDate.plusMonths(i);

                    // Check if rent for this month is paid
                    BigDecimal monthlyPaid = leasePayments.stream()
                            .filter(p -> p.getPaymentDate() != null &&
                                    p.getPaymentDate().getMonth() == dueDate.getMonth() &&
                                    p.getPaymentDate().getYear() == dueDate.getYear())
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    if (monthlyPaid.compareTo(rentAmount) < 0) {
                        BigDecimal unpaid = rentAmount.subtract(monthlyPaid);
                        long daysOverdue = today.toEpochDay() - dueDate.toEpochDay();

                        if (daysOverdue <= 0) {
                            current = current.add(unpaid);
                        } else if (daysOverdue <= 30) {
                            days1_30 = days1_30.add(unpaid);
                        } else if (daysOverdue <= 60) {
                            days31_60 = days31_60.add(unpaid);
                        } else if (daysOverdue <= 90) {
                            days61_90 = days61_90.add(unpaid);
                        } else {
                            over90 = over90.add(unpaid);
                        }
                    }
                }
            }
        }

        BigDecimal total = current.add(days1_30).add(days31_60).add(days61_90).add(over90);

        return AccountsReceivableDTO.builder()
                .total(total.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .current(current.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .days1_30(days1_30.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .days31_60(days31_60.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .days61_90(days61_90.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .over90(over90.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .branchId(branchId)
                .build();
    }

    @Override
    public CashFlowDataDTO getCashFlowData(Integer branchId) {
        int currentYear = LocalDate.now().getYear();
        List<Object[]> monthlyData = branchRepository.getMonthlyRevenue(currentYear, branchId);

        Map<Integer, BigDecimal> monthAmountMap = new HashMap<>();
        for (Object[] data : monthlyData) {
            Integer month = (Integer) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            monthAmountMap.put(month, amount);
        }

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            labels.add(MONTHS[i]);
            BigDecimal amount = monthAmountMap.getOrDefault(i + 1, BigDecimal.ZERO);
            values.add(amount.setScale(2, RoundingMode.HALF_UP).doubleValue());
        }

        return CashFlowDataDTO.builder()
                .labels(labels)
                .values(values)
                .branchId(branchId)
                .build();
    }

    @Override
    public InvoiceDataDTO getInvoiceData(Integer branchId) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        // Unpaid (PENDING payments)
        BigDecimal unpaid = branchRepository.getPendingAmountByDueDate(LocalDate.now(), branchId);

        // Overdue (OVERDUE payments)
        BigDecimal overdue = branchRepository.getTotalOverdueAmount(branchId);

        // Not due yet (PENDING payments with future due date - simplified)
        BigDecimal notDue = BigDecimal.ZERO; // You'd need to calculate this based on lease due dates

        // Paid in last 30 days
        BigDecimal paid = branchRepository.getRevenueInPeriod(thirtyDaysAgo, LocalDate.now(), branchId);

        // For simplicity, assuming 70% deposited, 30% not deposited
        BigDecimal notDeposited = paid.multiply(BigDecimal.valueOf(0.3));
        BigDecimal deposited = paid.multiply(BigDecimal.valueOf(0.7));

        // Last 365 days statistics (simplified)
        Long totalLast365 = branchRepository.countNewLeases(oneYearAgo, branchId);
        long overdueLast365 = totalLast365 / 5; // Assuming 20% are overdue

        return InvoiceDataDTO.builder()
                .unpaid(unpaid.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .overdue(overdue.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .notDue(notDue.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .paid(paid.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .notDeposited(notDeposited.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .deposited(deposited.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .last365Days(new InvoiceDataDTO.Last365Days(totalLast365.intValue(), (int) overdueLast365))
                .branchId(branchId)
                .build();
    }

    @Override
    public List<ExpenseDataDTO> getExpenseData(Integer branchId) {
        List<ExpenseDataDTO> expenses = new ArrayList<>();

        // Get last 7 months
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String period = month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            // Calculate expenses for each category (simplified)
            BigDecimal maintenance = branchRepository.getMaintenanceCosts(branchId)
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            BigDecimal utilities = branchRepository.getUtilityCosts(branchId)
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            BigDecimal salaries = BigDecimal.valueOf(5000); // Fixed assumption

            // Rotate categories for demo
            String category;
            BigDecimal amount;
            if (i % 3 == 0) {
                category = "Maintenance";
                amount = maintenance;
            } else if (i % 3 == 1) {
                category = "Utilities";
                amount = utilities;
            } else {
                category = "Staff Salaries";
                amount = salaries;
            }

            // Add some variation
            double variation = 0.8 + (Math.random() * 0.4); // 0.8 to 1.2
            amount = amount.multiply(BigDecimal.valueOf(variation));

            expenses.add(ExpenseDataDTO.builder()
                    .period(period)
                    .amount(amount.setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .category(category)
                    .branchId(branchId)
                    .build());
        }

        return expenses;
    }

    @Override
    public XeroMetricsDTO getXeroMetrics(Integer branchId) {
        // Accounts Receivable (from our calculation)
        AccountsReceivableDTO ar = getAccountsReceivable(branchId);

        // Accounts Payable (simplified - assuming 60% of revenue is payable)
        BigDecimal totalRevenue = branchRepository.getTotalRevenue(branchId);
        BigDecimal accountsPayable = totalRevenue.multiply(BigDecimal.valueOf(0.6));

        // Monthly Income (last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        BigDecimal monthlyIncome = branchRepository.getRevenueInPeriod(thirtyDaysAgo, LocalDate.now(), branchId);

        // Bank Balance (simplified - assuming 40% of total revenue is in bank)
        BigDecimal bankBalance = totalRevenue.multiply(BigDecimal.valueOf(0.4));

        return XeroMetricsDTO.builder()
                .accountsReceivable(ar.getTotal())
                .accountsPayable(accountsPayable.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .income(monthlyIncome.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .bankBalance(bankBalance.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .branchId(branchId)
                .build();
    }

    @Override
    public List<PaymentTargetDataDTO> getPaymentTargetData(Integer branchId) {
        List<PaymentTargetDataDTO> targets = new ArrayList<>();

        // Get pending and overdue payments
        List<Payment> pendingPayments = branchRepository.getPendingAndOverduePayments(branchId);

        // Group by aging
        Map<String, BigDecimal> receivableMap = new HashMap<>();
        Map<String, BigDecimal> payableMap = new HashMap<>();

        // Initialize categories
        String[] categories = {"Current", "1–7 days", "8–30 days", "31–60 days", "61–90 days", "Over 90"};
        for (String category : categories) {
            receivableMap.put(category, BigDecimal.ZERO);
            payableMap.put(category, BigDecimal.ZERO);
        }

        LocalDate today = LocalDate.now();

        for (Payment payment : pendingPayments) {
            if (payment.getPaymentDate() == null) continue;

            long daysOverdue = today.toEpochDay() - payment.getPaymentDate().toEpochDay();
            String category;

            if (daysOverdue <= 0) {
                category = "Current";
            } else if (daysOverdue <= 7) {
                category = "1–7 days";
            } else if (daysOverdue <= 30) {
                category = "8–30 days";
            } else if (daysOverdue <= 60) {
                category = "31–60 days";
            } else if (daysOverdue <= 90) {
                category = "61–90 days";
            } else {
                category = "Over 90";
            }

            if (payment.getLease() != null) {
                // Receivable (rent payments)
                receivableMap.computeIfPresent(category, (k, current) -> current.add(payment.getAmount()));
            } else if (payment.getUtility() != null) {
                // Payable (utility payments)
                payableMap.computeIfPresent(category, (k, current) -> current.add(payment.getAmount()));
            }
        }

        // Create DTOs
        for (String category : categories) {
            targets.add(PaymentTargetDataDTO.builder()
                    .age(category)
                    .receivable(receivableMap.get(category).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .payable(payableMap.get(category).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .branchId(branchId)
                    .build());
        }

        return targets;
    }

    @Override
    public List<TimeSeriesDataDTO> getTimeSeriesData(Integer branchId) {
        List<TimeSeriesDataDTO> timeSeries = new ArrayList<>();

        // Get last 6 months
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthName = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            // Calculate receivable and payable for each month
            YearMonth yearMonth = YearMonth.from(month);
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();

            // Receivable (paid rent in that month)
            BigDecimal receivable = branchRepository.getRevenueInPeriod(startOfMonth, endOfMonth, branchId);

            // Payable (simplified - assume 60% of receivable is payable)
            BigDecimal payable = receivable.multiply(BigDecimal.valueOf(0.6));

            // Add random variation for demo
            double receivableVariation = 0.7 + (Math.random() * 0.6);
            double payableVariation = 0.7 + (Math.random() * 0.6);

            receivable = receivable.multiply(BigDecimal.valueOf(receivableVariation));
            payable = payable.multiply(BigDecimal.valueOf(payableVariation));

            timeSeries.add(TimeSeriesDataDTO.builder()
                    .month(monthName)
                    .receivable(receivable.setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .payable(payable.setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .branchId(branchId)
                    .build());
        }

        return timeSeries;
    }

    @Override
    public RentalMetricsDTO getRentalAnalytics(Integer branchId) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);

        // Total Revenue (last 30 days)
        BigDecimal totalRevenue = branchRepository.getRevenueInPeriod(thirtyDaysAgo, LocalDate.now(), branchId);

        // Occupancy Rate
        Long totalSpaces = branchRepository.countTotalSpaces(branchId);
        Long occupiedSpaces = branchRepository.countOccupiedSpaces(branchId);
        double occupancyRate = totalSpaces > 0 ? (occupiedSpaces.doubleValue() / totalSpaces.doubleValue()) * 100 : 0;

        // Average Daily Rate (simplified)
        BigDecimal avgRent = branchRepository.getAverageRent(branchId);
        BigDecimal averageDailyRate = avgRent.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        // New Bookings (leases started in last 30 days)
        Long newBookings = branchRepository.countNewLeases(thirtyDaysAgo, branchId);

        // Extensions (simplified - assume 30% of active leases were extended)
        List<Lease> activeLeases = branchRepository.getActiveLeases(branchId);
        long extensions = Math.round(activeLeases.size() * 0.3);

        // Cancellations (expired leases in last 30 days)
        Long cancellations = branchRepository.countExpiredLeases(thirtyDaysAgo, branchId);

        // Maintenance Costs
        BigDecimal maintenanceCosts = branchRepository.getMaintenanceCosts(branchId)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP); // Monthly

        // Utility Collections (simplified)
        BigDecimal utilityCollections = branchRepository.getUtilityCosts(branchId)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP); // Monthly

        return RentalMetricsDTO.builder()
                .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .occupancyRate((int) Math.round(occupancyRate))
                .averageDailyRate(averageDailyRate.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .newBookings(newBookings.intValue())
                .extensions((int) extensions)
                .cancellations(cancellations.intValue())
                .maintenanceCosts(maintenanceCosts.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .utilityCollections(utilityCollections.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .branchId(branchId)
                .build();
    }

    private BigDecimal calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return newValue.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}