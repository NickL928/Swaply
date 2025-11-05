package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.UserDto;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.service.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {

        if (userDto.getUserName() == null || userDto.getUserName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is required");
        }
        if (userDto.getEmail() == null || !userDto.getEmail().contains("@")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Valid email is required");
        }

        // Convert UserDto to User entity
        User user = new User();
        user.setUserName(userDto.getUserName());
        user.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        user.setProfileImageUrl(userDto.getProfileImageUrl());

        try {
            userService.addUser(user);
            UserDto created = new UserDto(user.getUserId(), user.getUserName(), user.getEmail());
            created.setProfileImageUrl(user.getProfileImageUrl());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDto userDto = new UserDto(user.getUserId(), user.getUserName(), user.getEmail());
            userDto.setProfileImageUrl(user.getProfileImageUrl());
            return ResponseEntity.ok(userDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            User userToUpdate = userOpt.get();
            userToUpdate.setUserName(userDto.getUserName());
            userToUpdate.setEmail(userDto.getEmail());
            if (userDto.getProfileImageUrl() != null) {
                userToUpdate.setProfileImageUrl(userDto.getProfileImageUrl());
            }
            // Only update password if it's provided
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                userToUpdate.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            try {
                userService.updateUser(userToUpdate);
                return ResponseEntity.ok("User updated successfully");
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

    }

    @GetMapping ("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        Optional <User> user = userService.getUserByUserName(username);
        if (user.isPresent()) {
            User foundUser = user.get();
            UserDto userDto = new UserDto(foundUser.getUserId(), foundUser.getUserName(), foundUser.getEmail());
            userDto.setProfileImageUrl(foundUser.getProfileImageUrl());
            return ResponseEntity.ok(userDto);
        } else {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            User foundUser = user.get();
            UserDto userDto = new UserDto(foundUser.getUserId(), foundUser.getUserName(), foundUser.getEmail());
            userDto.setProfileImageUrl(foundUser.getProfileImageUrl());
            return ResponseEntity.ok(userDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


}
