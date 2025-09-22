package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.UserDto;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service       //bean
public class UserService implements IUserService{

    @Autowired
    UserRepository userRepository;

    @Override
    public void add(UserDto user) {

        User userPojo = new User();

        BeanUtils.copyProperties(user, userPojo);

        userRepository.save(userPojo);
    }
}
