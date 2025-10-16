package com.spring.Services;
import com.spring.DTO.request.UpdateUserRequestDTO;
import com.spring.Entity.User;
import com.spring.Exceptions.UserException;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User findUserById(UUID id) throws UserException;

    User findUserByProfile(String jwt) throws UserException;

    User updateUser(UUID id, UpdateUserRequestDTO request) throws UserException;

    List<User> searchUser(String query);

    List<User> searchUserByName(String name);

}

