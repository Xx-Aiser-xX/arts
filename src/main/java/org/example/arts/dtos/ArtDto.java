package org.example.arts.dtos;

import org.example.arts.entities.User;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ArtDto implements Serializable {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private int countLikes;
    private int countViews;
    private boolean nsfw;
    private LocalDateTime publicationTime;
    private UserMinDto author;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
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

    public boolean isNsfw() {
        return nsfw;
    }
    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public LocalDateTime getPublicationTime() {
        return publicationTime;
    }
    public void setPublicationTime(LocalDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }

    public UserMinDto getAuthor() {
        return author;
    }

    public void setAuthor(UserMinDto author) {
        this.author = author;
    }
}
