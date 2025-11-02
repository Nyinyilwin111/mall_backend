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

import java.util.List;
import java.util.Set;

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
    public ResponseEntity<Set<UserDTO>> searchUsersByName(@RequestParam(value = "name", required = false) String name) {

        // Handle case when no name parameter is provided
        if (name == null || name.trim().isEmpty()) {
            System.out.println("❌ No search name provided, returning empty results");
            return new ResponseEntity<>(Set.of(), HttpStatus.OK);
        }

        String searchTerm = name.trim();
        System.out.println("🔍 Searching users with term: '" + searchTerm + "'");

        List<User> users = userService.searchUserByName(searchTerm);
        System.out.println("📊 Found " + users.size() + " users matching '" + searchTerm + "'");

        if (users.isEmpty()) {
            System.out.println("💡 No users found. Available users in database:");
            // List all users for debugging
            List<User> allUsers = userService.findAllUsers(); // You'll need to add this method
            allUsers.forEach(user -> System.out.println("   - " + user.getFullName() + " | " + user.getEmail()));
        }

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

}
