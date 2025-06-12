package org.example.arts.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class ArtCardDto {
    private UUID id;
    private String name;
    private String imageUrl;
    private int countLikes;
    private int countViews;
    private LocalDateTime publicationTime;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

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
