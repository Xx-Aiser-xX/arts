package org.example.arts.dtos.create;

import org.example.arts.entities.Art;
import org.example.arts.entities.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommentCreateDto {
    private UUID id;
    private Art art;
    private String text;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Art getArt() {
        return art;
    }

    public void setArt(Art art) {
        this.art = art;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
