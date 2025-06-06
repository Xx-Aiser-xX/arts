package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

@Entity
@Table(name = "social_networks")
public class SocialNetwork extends BaseEntityId {
    private User user;
    private String link;
//    private String platform;
    private boolean deleted;

    protected SocialNetwork() {}

    public SocialNetwork(User user, String link) {
        setUser(user);
        setLink(link);
//        setPlatform(platform);
        setDeleted(false);
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

    @Column(name = "link", nullable = false, length = 2000)
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        if (link == null || link.length() > 2000)
            throw new IncorrectDataException(link);
        this.link = link;
    }

//    @Column(name = "platform", nullable = false, length = 50)
//    public String getPlatform() {
//        return platform;
//    }
//    public void setPlatform(String platform) {
//        if (platform == null || platform.length() > 50)
//            throw new IncorrectDataException(platform);
//        this.platform = platform;
//    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
