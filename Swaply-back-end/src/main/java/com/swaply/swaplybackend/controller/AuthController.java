package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.LoginRequestDto;
import com.swaply.swaplybackend.dto.LoginResponseDto;
import com.swaply.swaplybackend.dto.UserDto;
import com.swaply.swaplybackend.enums.UserRole;
import com.swaply.swaplybackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        System.out.println("DEBUG: Received userName=" + loginRequest.getUserName());
        System.out.println("DEBUG: Received password=" + loginRequest.getPassword());
        try {
            LoginResponseDto response = authService.login(loginRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto userDto) {
        userDto.setRole(UserRole.USER);

        try {
            UserDto createdUser = authService.register(userDto, userDto.getPassword());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
