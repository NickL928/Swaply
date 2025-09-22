package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.UserDto;
import com.swaply.swaplybackend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    IUserService userService;

    @PostMapping
    public String add(@RequestBody UserDto user){
        userService.add(user);
        return "success";
    }

}
