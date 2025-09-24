package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User saveUser(User user);
    Optional<User> getUserById(long userId);
    List<User> getAllUsers();
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(long userId);
    void deleteUser(User user);

    Optional<User> getUserByUserName(String username);

    Optional<User> getUserByEmail(String email);
}
