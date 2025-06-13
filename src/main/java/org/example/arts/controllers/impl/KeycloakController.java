package org.example.arts.controllers.impl;

import org.example.arts.dtos.create.KeycloakRegisterDto;
import org.example.arts.services.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.arts.dtos.KeycloakUserDto;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/keycloak")
public class KeycloakController {

    private final KeycloakService keycloakService;

    @Autowired
    public KeycloakController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @PostMapping("/register")
    public ResponseEntity<KeycloakUserDto> registerUser(@RequestBody KeycloakRegisterDto request) {
        KeycloakUserDto user = keycloakService.registerUser(request);
        return ResponseEntity.ok(user);
    }
}
