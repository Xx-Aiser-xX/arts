package org.example.arts.dtos.create;

import org.example.arts.entities.Art;
import org.example.arts.entities.User;

import java.util.UUID;

public class InteractionCreateDto {
    private UUID id;
    private User user;
    private Art art;
}
