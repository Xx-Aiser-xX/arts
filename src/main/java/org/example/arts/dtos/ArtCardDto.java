package org.example.arts.dtos;

import org.example.arts.entities.User;

import java.time.LocalDateTime;

public class ArtCardDto {
    private String name;
    private String imageUrl;
    private int countLikes;
    private int countViews;
    private LocalDateTime publicationTime;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCountLikes() {
        return countLikes;
    }
    public void setCountLikes(int countLikes) {
        this.countLikes = countLikes;
    }

    public int getCountViews() {
        return countViews;
    }
    public void setCountViews(int countViews) {
        this.countViews = countViews;
    }

    public LocalDateTime getPublicationTime() {
        return publicationTime;
    }
    public void setPublicationTime(LocalDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }
}
