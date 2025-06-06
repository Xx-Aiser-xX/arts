package org.example.arts.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferences extends BaseEntityId {
    private User user;
    private boolean deleted;


    protected UserPreferences() {
    }

    public UserPreferences(User user) {
        this.user = user;
        setDeleted(false);
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
