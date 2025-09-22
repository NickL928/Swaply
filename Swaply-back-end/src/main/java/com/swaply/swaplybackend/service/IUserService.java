package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.UserDto;

public interface IUserService {
    /**
     * insert user
     * @param user
     */
    void add(UserDto user);
}
