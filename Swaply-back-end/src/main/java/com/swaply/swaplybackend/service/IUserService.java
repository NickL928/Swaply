package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User saveUser(User user);
    Optional<User> getUserById(Long userId);
    List<User> getAllUsers();
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(Long userId);
    void deleteUser(User user);

    Optional<User> getUserByUserName(String username);

    Optional<User> getUserByEmail(String email);
}
