package org.example.arts.controllers.impl;

import org.example.arts.dtos.create.RegisterUserDto;
import org.example.arts.dtos.UserDto;
import org.example.arts.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestBody RegisterUserDto request) {
        UserDto user = userService.registerUser(request);
        return ResponseEntity.ok(user);
    }
}
