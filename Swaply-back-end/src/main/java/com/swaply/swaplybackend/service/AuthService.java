package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.LoginRequestDto;
import com.swaply.swaplybackend.dto.LoginResponseDto;
import com.swaply.swaplybackend.dto.UserDto;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.Util.JwtUtil;
import com.swaply.swaplybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDto login(LoginRequestDto loginRequest) {

        //find user by username
        Optional<User> userOptional = userRepository.findByUserName(loginRequest.getUserName());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Username not found");
        }

        User user = userOptional.get();

        //check password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        //generate token
        String userRole = (user.getRole() != null) ? user.getRole().name() : "USER";
        String token = jwtUtil.generateToken(user.getUserName(), userRole);

        //convert to userDto
        UserDto userDto = convertToUserDto(user);

        return new LoginResponseDto(token, userDto);
    }

    public UserDto register(UserDto userDto, String password) {

        //check if username already exists
        if (userRepository.findByUserName(userDto.getUserName()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        //check if email already exists
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        //create new user
        User user = new User();
        user.setUserName(userDto.getUserName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(userDto.getRole());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return convertToUserDto(savedUser);
    }

    private  UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUserName(user.getUserName());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setIsActive(user.getIsActive());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());

        return userDto;
    }


}
