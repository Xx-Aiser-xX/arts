package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

@Entity
@Table(name = "art_tags", indexes = {
        @Index(name = "idx_art_tags_deleted_art", columnList = "is_deleted, art_id"),
        @Index(name = "idx_art_tags_deleted_tag", columnList = "is_deleted, tag_id")
})
public class ArtTag extends BaseEntityId {
    private Art art;
    private Tag tag;
    private boolean deleted;

    protected ArtTag() {}

    public ArtTag(Art art, Tag tag) {
        setArt(art);
        setTag(tag);
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
    @JoinColumn(name = "tag_id", referencedColumnName = "id")
    public Tag getTag() {
        return tag;
    }
    public void setTag(Tag tag) {
        if (tag == null)
            throw new IncorrectDataException("tag is null");
        this.tag = tag;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}