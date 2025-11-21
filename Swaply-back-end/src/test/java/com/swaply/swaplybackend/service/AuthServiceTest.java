package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.Util.JwtUtil;
import com.swaply.swaplybackend.dto.LoginRequestDto;
import com.swaply.swaplybackend.dto.LoginResponseDto;
import com.swaply.swaplybackend.dto.UserDto;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.UserRole;
import com.swaply.swaplybackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;

    @InjectMocks AuthService authService;

    private User existingUser;

    @BeforeEach
    void setup() {
        existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setUserName("nick");
        existingUser.setEmail("nick@swaply.com");
        existingUser.setPassword("$2a$10$encodedPassword");
        existingUser.setRole(UserRole.USER);
        existingUser.setIsActive(true);
    }

    @Test
    void login_withValidUsername_returnsTokenAndUser() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("nick");
        request.setPassword("110531");

        when(userRepository.findByUserName("nick")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("110531", existingUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("nick", "USER")).thenReturn("fake-jwt-token");

        LoginResponseDto response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUser().getUserName()).isEqualTo("nick");
        assertThat(response.getUser().getUserId()).isEqualTo(1L);
        verify(jwtUtil).generateToken("nick", "USER");
    }

    @Test
    void login_withValidEmail_fallsBackToEmailLookup() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("nick@swaply.com");
        request.setPassword("110531");

        when(userRepository.findByUserName("nick@swaply.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nick@swaply.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("110531", existingUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("nick", "USER")).thenReturn("token");

        LoginResponseDto response = authService.login(request);

        assertThat(response.getUser().getEmail()).isEqualTo("nick@swaply.com");
        verify(userRepository).findByEmail("nick@swaply.com");
    }

    @Test
    void login_withWrongPassword_throwsException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("nick");
        request.setPassword("wrongpass");

        when(userRepository.findByUserName("nick")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpass", existingUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void login_withUnknownUser_throwsException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("unknown");
        request.setPassword("anypass");

        when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void register_withNewUser_createsAndReturnsUser() {
        UserDto newUser = new UserDto();
        newUser.setUserName("buyer");
        newUser.setEmail("buyer@swaply.com");
        newUser.setRole(UserRole.USER);

        User savedUser = new User();
        savedUser.setUserId(2L);
        savedUser.setUserName("buyer");
        savedUser.setEmail("buyer@swaply.com");
        savedUser.setPassword("$2a$10$encodedBuyerPass");
        savedUser.setRole(UserRole.USER);
        savedUser.setIsActive(true);

        when(userRepository.findByUserName("buyer")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("buyer@swaply.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Buyer123!")).thenReturn("$2a$10$encodedBuyerPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = authService.register(newUser, "Buyer123!");

        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getUserName()).isEqualTo("buyer");
        verify(userRepository).save(argThat(user ->
                user.getUserName().equals("buyer") &&
                user.getIsActive() &&
                user.getRole() == UserRole.USER));
    }

    @Test
    void register_withExistingUsername_throwsException() {
        UserDto dto = new UserDto();
        dto.setUserName("nick");
        dto.setEmail("newemail@test.com");

        when(userRepository.findByUserName("nick")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(dto, "pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void register_withExistingEmail_throwsException() {
        UserDto dto = new UserDto();
        dto.setUserName("newuser");
        dto.setEmail("nick@swaply.com");

        when(userRepository.findByUserName("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nick@swaply.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(dto, "pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }
}

