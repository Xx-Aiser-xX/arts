package org.example.arts.dtos;

import org.example.arts.entities.Art;
import org.example.arts.entities.User;

import java.time.LocalDateTime;

public class CommentDto {
    private String id;
    private String authorId;
    private String authorUserName;
    private String authorPhotoUrl;
    private String text;
    private LocalDateTime publicationTime;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUserName() {
        return authorUserName;
    }
    public void setAuthorUserName(String authorUserName) {
        this.authorUserName = authorUserName;
    }

    public String getAuthorPhotoUrl() {
        return authorPhotoUrl;
    }
    public void setAuthorPhotoUrl(String authorPhotoUrl) {
        this.authorPhotoUrl = authorPhotoUrl;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getPublicationTime() {
        return publicationTime;
    }
    public void setPublicationTime(LocalDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }
}
