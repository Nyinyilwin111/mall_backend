package com.spring.Services.ServiceImplements;

import com.spring.DTO.request.UpdateUserRequestDTO;
import com.spring.DTO.response.BranchResponseDTO;
import com.spring.DTO.response.UserResponseDTO;
import com.spring.Entity.Branch;
import com.spring.Entity.User;
import com.spring.Exceptions.UserException;
import com.spring.Repository.UserRepository;
import com.spring.Services.UserService;
import com.spring.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImplement implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;    

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user: " + username);

        User user = userRepository.findByFullName(username);

        System.out.println("User found: " + user.getFullName() + ", enabled: " + user.isEnabled());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getFullName())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled()) // Important: check if user is enabled
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("No roles found for user: " + user.getFullName());
            return java.util.Collections.emptySet();
        }

        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet());

        System.out.println("Authorities for user " + user.getFullName() + ": " + authorities);
        return authorities;
    }

    @Override
    public List<User> getAllUsersWithRoles() {
        return userRepository.findAllWithRoles();
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }

    @Override
    public User findUserById(UUID id) throws UserException {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return user.get();
        }

        throw new UserException("User not found with id " + id);
    }

    // In UserServiceImplement.java - FIX THIS METHOD
    @Override
    public User findUserByProfile(String username) throws UserException {
        System.out.println("🔍 Finding user by profile: " + username);

        // Try to find by email first
        Optional<User> userByEmail = userRepository.findByEmail(username);
        if (userByEmail.isPresent()) {
            System.out.println("✅ User found by email: " + username);
            return userByEmail.get();
        }

        // If not found by email, try by username/fullName
        User userByFullName = userRepository.findByFullName(username);
        if (userByFullName != null) {
            System.out.println("✅ User found by fullName: " + username);
            return userByFullName;
        }

        System.out.println("❌ User not found: " + username);
        throw new UserException("User not found: " + username);
    }

    @Override
    public User updateUser(UUID id, UpdateUserRequestDTO request) throws UserException {
        User user = findUserById(id);

        if (Objects.nonNull(request.fullName())) {
            user.setFullName(request.fullName());
        }

        return userRepository.save(user);
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.searchByFullNameOrEmail(query).stream()
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

    @Override
    public List<User> searchUserByName(String name) {
        return userRepository.searchByFullName(name).stream()
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByFullName(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByFullName(username);
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserResponseDTO getCurrentUserInfo(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmailWithRolesAndBranches(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return convertToUserResponseDTO(user);
    }

    // Add this method to your UserServiceImpl.java
    private UserResponseDTO convertToUserResponseDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFullName(user.getFullName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setEnabled(user.isEnabled());

        // Convert roles
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName()) // Assuming Role entity has getName() method
                .collect(Collectors.toSet());
        userResponseDTO.setRoles(roleNames);

        // Convert branches
        Set<BranchResponseDTO> branchDTOs = user.getBranches().stream()
                .map(this::convertToBranchResponseDTO)
                .collect(Collectors.toSet());
        userResponseDTO.setBranches(branchDTOs);

        return userResponseDTO;
    }

    // Also add this helper method if you don't have it
    private BranchResponseDTO convertToBranchResponseDTO(Branch branch) {
        BranchResponseDTO branchResponseDTO = new BranchResponseDTO();
        branchResponseDTO.setId(branch.getId());
        branchResponseDTO.setName(branch.getName());
        branchResponseDTO.setAddress(branch.getAddress());
        branchResponseDTO.setPhoneNumber(branch.getPhoneNumber());
        branchResponseDTO.setCreatedAt(branch.getCreatedAt());
        branchResponseDTO.setUpdatedAt(branch.getUpdatedAt());
        return branchResponseDTO;
    }
}
