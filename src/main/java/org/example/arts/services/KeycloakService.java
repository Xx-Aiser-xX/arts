package org.example.arts.services;

import org.example.arts.dtos.KeycloakUserDto;
import org.example.arts.dtos.create.KeycloakRegisterDto;

public interface KeycloakService {
    KeycloakUserDto registerUser(KeycloakRegisterDto request);
}
