package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.UpdateUserRequestDTO;
import com.sein_gar_har.dto.response.UserResponseDTO;
import com.sein_gar_har.entity.User;
import com.sein_gar_har.exception.UserException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User findUserById(UUID id) throws UserException;

    User findUserByProfile(String jwt) throws UserException;

    User updateUser(UUID id, UpdateUserRequestDTO request) throws UserException;

    List<User> searchUser(String query);

    List<User> searchUserByName(String name);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void save(User user);

    User findByUsername(String username) throws UsernameNotFoundException;

    List<User> getAllUsersWithRoles();

    List<User> findAll();

    Optional<User> findByEmail(String email);

    UserResponseDTO getCurrentUserInfo(Principal principal);

    // Add these new methods for audit log
    void deleteUser(UUID id) throws UserException;

    User toggleUserStatus(UUID id, boolean enabled) throws UserException;

    List<User> findByRole(String role);
}