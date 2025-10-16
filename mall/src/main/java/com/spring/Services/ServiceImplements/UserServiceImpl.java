package com.spring.Services.ServiceImplements;

import com.spring.Config.JwtConstants;
import com.spring.Config.TokenProvider;
import com.spring.DTO.request.UpdateUserRequestDTO;
import com.spring.Entity.User;
import com.spring.Exceptions.UserException;
import com.spring.Repository.UserRepository;
import com.spring.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
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
        return userRepository.findByFullName(name).stream()
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

}
