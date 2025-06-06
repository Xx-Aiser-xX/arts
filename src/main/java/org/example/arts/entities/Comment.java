package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntityId {
    private Art art;
    private User author;
    private String text;
    private LocalDateTime publicationTime;
    private boolean deleted;

    protected Comment() {}

    public Comment(Art art, User author, String text) {
        setArt(art);
        setAuthor(author);
        setText(text);
        setPublicationTime(LocalDateTime.now());
        setDeleted(false);
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "art_id", referencedColumnName = "id")
    public Art getArt() {
        return art;
    }
    public void setArt(Art art) {
        if (art == null)
            throw new IncorrectDataException("art is null");
        this.art = art;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        if (author == null)
            throw new IncorrectDataException("author is null");
        this.author = author;
    }

    @Column(name = "text", nullable = false, length = 200)
    public String getText() {
        return text;
    }
    public void setText(String text) {
        if (text == null || text.isEmpty() || text.length() > 200)
            throw new IncorrectDataException(text);
        this.text = text;
    }

    @Column(name = "publication_time", nullable = false)
    public LocalDateTime getPublicationTime() {
        return publicationTime;
    }
    public void setPublicationTime(LocalDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
