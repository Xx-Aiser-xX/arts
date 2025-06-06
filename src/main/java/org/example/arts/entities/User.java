package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User{
    private UUID id;
    private String userName;
    private String description;
    private String photoUrl;
    private int countJobs;
    private int countSubscriptions;
    private int countSubscribers;
    private boolean deleted;
    private Set<Art> arts;
    private Set<Interaction> interactions;
    private Set<Comment> comments;
    private Set<SocialNetwork> socialNetworks;
    private Set<Sub> subscribers;
    private Set<Sub> targets;
    private Set<UserPreferences> userPreferences;

    protected User() {}

    public User(UUID id, String userName, String photoUrl) {
        setId(id);
        setUserName(userName);
        setPhotoUrl(photoUrl);
        setCountJobs(0);
        setCountSubscriptions(0);
        setCountSubscribers(0);
        setDeleted(false);
    }

    @Id
    @Column(name = "id", updatable = false)
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    @Column(name = "user_name", nullable = false, length = 60, unique = true)
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        if (userName == null || userName.length() < 2 || userName.length() > 60)
            throw new IncorrectDataException(userName);
        this.userName = userName;
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

    @Column(name = "photo_url", length = 2000)
    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        if (photoUrl != null && photoUrl.length() > 2000)
            throw new IncorrectDataException(photoUrl);
        this.photoUrl = photoUrl;
    }

    @Column(name = "count_jobs", nullable = false)
    public int getCountJobs() {
        return countJobs;
    }
    public void setCountJobs(int countJobs) {
        if (countJobs < 0)
            throw new IncorrectDataException(String.valueOf(countJobs));
        this.countJobs = countJobs;
    }

    @Column(name = "count_subscriptions", nullable = false)
    public int getCountSubscriptions() {
        return countSubscriptions;
    }
    public void setCountSubscriptions(int countSubscriptions) {
        if (countSubscriptions < 0)
            throw new IncorrectDataException(String.valueOf(countSubscriptions));
        this.countSubscriptions = countSubscriptions;
    }

    @Column(name = "count_subscribers", nullable = false)
    public int getCountSubscribers() {
        return countSubscribers;
    }
    public void setCountSubscribers(int countSubscribers) {
        if (countSubscribers < 0)
            throw new IncorrectDataException(String.valueOf(countSubscribers));
        this.countSubscribers = countSubscribers;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    public Set<Art> getArts() {
        return arts;
    }
    public void setArts(Set<Art> arts) {
        this.arts = arts;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<Interaction> getInteractions() {
        return interactions;
    }
    public void setInteractions(Set<Interaction> interactions) {
        this.interactions = interactions;
    }

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    public Set<Comment> getComments() {
        return comments;
    }
    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<SocialNetwork> getSocialNetworks() {
        return socialNetworks;
    }
    public void setSocialNetworks(Set<SocialNetwork> socialNetworks) {
        this.socialNetworks = socialNetworks;
    }

    @OneToMany(mappedBy = "subscriber", fetch = FetchType.LAZY)
    public Set<Sub> getSubscribers() {
        return subscribers;
    }
    public void setSubscribers(Set<Sub> subscribers) {
        this.subscribers = subscribers;
    }

    @OneToMany(mappedBy = "target", fetch = FetchType.LAZY)
    public Set<Sub> getTargets() {
        return targets;
    }
    public void setTargets(Set<Sub> targets) {
        this.targets = targets;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<UserPreferences> getUserPreferences() {
        return userPreferences;
    }
    public void setUserPreferences(Set<UserPreferences> userPreferences) {
        this.userPreferences = userPreferences;
    }
}
