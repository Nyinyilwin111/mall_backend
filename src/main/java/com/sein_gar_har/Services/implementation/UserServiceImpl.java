package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.UserRepository;
import com.sein_gar_har.Services.UserService;
import com.sein_gar_har.config.JwtConstants;
import com.sein_gar_har.config.TokenProvider;
import com.sein_gar_har.dto.request.UpdateUserRequestDTO;
import com.sein_gar_har.dto.response.BranchResponseDTO;
import com.sein_gar_har.dto.response.UserResponseDTO;
import com.sein_gar_har.entity.Branch;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    private final TokenProvider tokenProvider;

    @Override
    public User findUserById(UUID id) throws UserException {

        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return user.get();
        }

        throw new UserException("User not found with id " + id);
    }

    @Override
    public User findUserByProfile(String jwt) throws UserException {

        String email = String.valueOf(tokenProvider.getClaimsFromToken(jwt).get(JwtConstants.EMAIL));

        if (email == null) {
            throw new BadCredentialsException("Invalid token");
        }

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return user.get();
        }

        throw new UserException("User not found with email " + email);
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
        return userRepository.findByFullNameOrEmail(query).stream()
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

    @Override
    public List<User> searchUserByName(String name) {
        return userRepository.findByName(name).stream()
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

//  this is new

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
    public User findByUsername(String username) throws UsernameNotFoundException {

        Optional<User> optionalUser = userRepository.findByFullName(username);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return optionalUser.get();
    }

    @Override
    public List<User> getAllUsersWithRoles() {
        return userRepository.findAllWithRoles();
    }

    // Additional method to check if any managers exist
    public boolean hasManagers() {
        List<User> managers = userRepository.findUsersByRole("MANAGER");
        return managers != null && !managers.isEmpty();
    }

    // Get count of managers
    public long getManagerCount() {
        return userRepository.findUsersByRole("MANAGER").size();
    }

    @Override
    public List<User> findAll() {
        return List.of();
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

    @Override
    public List<User> findByRole(String role) {
        return userRepository.findUsersByRole(role);
    }

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
