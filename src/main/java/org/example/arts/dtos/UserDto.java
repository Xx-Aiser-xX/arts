package org.example.arts.dtos;

public class UserDto {
    private String userName;
    private String description;
    private String photoUrl;
    private int countJobs;
    private int countSubscriptions;
    private int countSubscribers;

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getCountJobs() {
        return countJobs;
    }
    public void setCountJobs(int countJobs) {
        this.countJobs = countJobs;
    }

    public int getCountSubscriptions() {
        return countSubscriptions;
    }
    public void setCountSubscriptions(int countSubscriptions) {
        this.countSubscriptions = countSubscriptions;
    }

    public int getCountSubscribers() {
        return countSubscribers;
    }
    public void setCountSubscribers(int countSubscribers) {
        this.countSubscribers = countSubscribers;
    }
}
