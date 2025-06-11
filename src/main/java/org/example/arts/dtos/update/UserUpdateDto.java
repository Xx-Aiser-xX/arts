package org.example.arts.dtos.update;

import org.example.arts.dtos.SocialNetworkDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class UserUpdateDto {
    private String userName;
    private MultipartFile avatarFile;
    private String description;
    private List<SocialNetworkDto> socialNetwork;

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

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<SocialNetworkDto> getSocialNetwork() {
        return socialNetwork;
    }

    public void setSocialNetwork(List<SocialNetworkDto> socialNetwork) {
        this.socialNetwork = socialNetwork;
    }
}
