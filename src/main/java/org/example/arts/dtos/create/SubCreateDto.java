package org.example.arts.dtos.create;

import org.example.arts.entities.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class SubCreateDto {
    private UUID id;
    private User subscriber;
    private User target;
}
