package org.example.arts.dtos.create;

import java.util.UUID;

public class RegisterUserDto {
    private UUID id;
    private String userName;
    private String photoUrl;

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
