package org.example.arts.dtos;

import java.util.List;

public class UpdateUserDto {
    private String userName;
    private String photoUrl;
    private String description;
    private List<SocialNetworkDto> socialNetwork;

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
