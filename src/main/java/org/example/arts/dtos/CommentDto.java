package org.example.arts.dtos;

import org.example.arts.entities.Art;
import org.example.arts.entities.User;

import java.time.LocalDateTime;

public class CommentDto {
    private String authorId;
    private String authorUserName;
    private String text;
    private LocalDateTime publicationTime;

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
