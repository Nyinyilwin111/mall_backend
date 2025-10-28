package com.spring.Services;

import com.spring.DTO.request.UpdateUserRequestDTO;
import com.spring.Entity.User;
import com.spring.Exceptions.UserException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    User findUserById(UUID id) throws UserException;

    User findUserByProfile(String jwt) throws UserException;

    User updateUser(UUID id, UpdateUserRequestDTO request) throws UserException;

    List<User> searchUser(String query);

    List<User> searchUserByName(String name);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void save(User user);

    User findByUsername(String username);
}