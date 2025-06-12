package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions", indexes = {
        @Index(name = "idx_interactions_deleted_user_art", columnList = "is_deleted, user_id, art_id"),
        @Index(name = "idx_interactions_deleted_like_likedAt", columnList = "is_deleted, is_like, liked_at")
})
public class Interaction extends BaseEntityId {
    private User user;
    private Art art;
    private boolean like;
    private boolean view;
    private boolean deleted;
    private LocalDateTime likedAt;
    private LocalDateTime viewAt;

    protected Interaction() {}

    public Interaction(User user, Art art) {
        setUser(user);
        setArt(art);
        setLike(false);
        setView(true);
        setDeleted(false);
        setViewAt(LocalDateTime.now());
        setViewAt(null);
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        if (user == null)
            throw new IncorrectDataException("user is null");
        this.user = user;
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

    @Column(name = "is_like", nullable = false)
    public boolean isLike() {
        return like;
    }
    public void setLike(boolean like) {
        this.like = like;
    }

    @Column(name = "is_view", nullable = false)
    public boolean isView() {
        return view;
    }
    public void setView(boolean view) {
        this.view = view;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Column(name = "liked_at")
    public LocalDateTime getLikedAt() {
        return likedAt;
    }
    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }

    @Column(name = "view_at")
    public LocalDateTime getViewAt() {
        return viewAt;
    }
    public void setViewAt(LocalDateTime viewAt) {
        this.viewAt = viewAt;
    }
}
