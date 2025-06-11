package org.example.arts.dtos.create;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class RegisterUserDto {
    private UUID id;
    private String userName;
    private MultipartFile avatarFile;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public MultipartFile getAvatarFile() {
        return avatarFile;
    }

    public void setAvatarFile(MultipartFile avatarFile) {
        this.avatarFile = avatarFile;
    }
}
