package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "arts", indexes = {
        @Index(name = "idx_arts_deleted_user_id", columnList = "is_deleted, user_id"),
        @Index(name = "idx_arts_deleted_publication_time", columnList = "is_deleted, publication_time")
})
public class Art extends BaseEntityId {
    private String name;
    private String description;
    private String imageUrl;
    private int countLikes;
    private int countViews;
    private boolean nsfw;
    private LocalDateTime publicationTime;
    private User author;
    private boolean deleted;
    private Set<ArtTag> artTagSet;
    private Set<Comment> comments;
    private Set<Interaction> interactions;

    protected Art() {}

    public Art(String name, String description, String imageUrl, boolean nsfw, User author) {
        setName(name);
        setDescription(description);
        setImageUrl(imageUrl);
        setCountLikes(0);
        setCountViews(0);
        setNsfw(nsfw);
        setPublicationTime(LocalDateTime.now());
        setAuthor(author);
        setDeleted(false);
    }

    @Column(name = "name", nullable = false, unique = true, length = 200)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        if (name == null || name.isEmpty() || name.length() > 200)
            throw new IncorrectDataException(name);
        this.name = name;
    }

    @Column(name = "description", columnDefinition = "TEXT")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        if (description != null && description.length() > 5000)
            throw new IncorrectDataException(description);
        this.description = description;
    }

    @Column(name = "image_url", unique = true, nullable = false, length = 2000)
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.length() > 2000)
            throw new IncorrectDataException(imageUrl);
        this.imageUrl = imageUrl;
    }

    @Column(name = "count_likes", nullable = false)
    public int getCountLikes() {
        return countLikes;
    }
    public void setCountLikes(int countLikes) {
        if (countLikes < 0)
            throw new IncorrectDataException(String.valueOf(countLikes));
        this.countLikes = countLikes;
    }

    @Column(name = "count_views", nullable = false)
    public int getCountViews() {
        return countViews;
    }
    public void setCountViews(int countViews) {
        if (countViews < 0)
            throw new IncorrectDataException(String.valueOf(countViews));
        this.countViews = countViews;
    }

    @Column(name = "nsfw")
    public boolean isNsfw() {
        return nsfw;
    }
    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Column(name = "publication_time", nullable = false)
    public LocalDateTime getPublicationTime() {
        return publicationTime;
    }
    public void setPublicationTime(LocalDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        if (author == null)
            throw new IncorrectDataException("author is null");
        this.author = author;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }


    @OneToMany(mappedBy = "art", fetch = FetchType.LAZY)
    public Set<ArtTag> getArtTagSet() {
        return artTagSet;
    }
    public void setArtTagSet(Set<ArtTag> artTagSet) {
        this.artTagSet = artTagSet;
    }

    @OneToMany(mappedBy = "art", fetch = FetchType.LAZY)
    public Set<Comment> getComments() {
        return comments;
    }
    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    @OneToMany(mappedBy = "art", fetch = FetchType.LAZY)
    public Set<Interaction> getInteractions() {
        return interactions;
    }
    public void setInteractions(Set<Interaction> interactions) {
        this.interactions = interactions;
    }
}