package com.spring.Controller;

import com.spring.Config.JwtConstants;
import com.spring.DTO.request.UpdateUserRequestDTO;
import com.spring.DTO.response.ApiResponseDTO;
import com.spring.DTO.response.UserDTO;
import com.spring.Entity.User;
import com.spring.Exceptions.UserException;
import com.spring.Services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@RequestHeader(JwtConstants.TOKEN_HEADER) String token) throws UserException {

        // remove "Bearer " if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            System.out.println("token ---- = "+token);
        }
        User user = userService.findUserByProfile(token);
        return new ResponseEntity<>(UserDTO.fromUser(user), HttpStatus.OK);
    }

    @GetMapping("/{query}")
    public ResponseEntity<List<UserDTO>> searchUsers(@PathVariable String query) {

        List<User> users = userService.searchUser(query);

        return new ResponseEntity<>(UserDTO.fromUsersAsList(users), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Set<UserDTO>> searchUsersByName(@RequestParam("name") String name) {

        List<User> users = userService.searchUserByName(name);

        return new ResponseEntity<>(UserDTO.fromUsers(users), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponseDTO> updateUser(@RequestBody UpdateUserRequestDTO request,
                                                     @RequestHeader(JwtConstants.TOKEN_HEADER) String token)
            throws UserException {

        User user = userService.findUserByProfile(token);
        user = userService.updateUser(user.getId(), request);
        log.info("User updated: {}", user.getEmail());

        ApiResponseDTO response = ApiResponseDTO.builder()
                .message("User updated")
                .status(true)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Add this to your UserController.java
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsersWithRoles(); // Use the new method
        return new ResponseEntity<>(UserDTO.fromUsersAsList(users), HttpStatus.OK);
    }

    @GetMapping("/debug-all-users")
    public ResponseEntity<List<Map<String, Object>>> debugAllUsers() {
        List<User> users = userService.findAll();
        List<Map<String, Object>> userInfo = users.stream()
                .map(user -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", user.getId());
                    info.put("email", user.getEmail());
                    info.put("fullName", user.getFullName());
                    info.put("enabled", user.isEnabled());
                    info.put("roles", user.getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toList()));
                    info.put("branches", user.getBranches().stream()
                            .map(branch -> branch.getName())
                            .collect(Collectors.toList()));
                    return info;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(userInfo);
    }
}
