package org.example.arts.dtos;

import org.example.arts.entities.Art;
import org.example.arts.entities.Tag;

public class ArtTagDto {
    private Art art;
    private Tag tag;

    public Art getArt() {
        return art;
    }

    public void setArt(Art art) {
        this.art = art;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
