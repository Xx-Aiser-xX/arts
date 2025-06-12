package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

import java.util.Set;

@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tags_name_deleted", columnList = "name, is_deleted")
})
public class Tag extends BaseEntityId {
    private String name;
    private boolean deleted;
    private Set<ArtTag> artTags;

    protected Tag() {}

    public Tag(String name) {
        setName(name);
        setDeleted(false);
    }

    @Column(name = "name", nullable = false, unique = true, length = 100)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        if (name == null || name.isEmpty() || name.length() > 100)
            throw new IncorrectDataException(name);
        this.name = name;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    public Set<ArtTag> getArtTags() {
        return artTags;
    }
    public void setArtTags(Set<ArtTag> artTags) {
        this.artTags = artTags;
    }
}