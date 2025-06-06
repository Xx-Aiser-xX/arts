package org.example.arts.dtos.create;

import org.example.arts.dtos.TagDto;
import org.example.arts.entities.User;

import java.time.LocalDateTime;
import java.util.List;

public class ArtCreateDto {
    private String name;
    private String description;
    private String imageUrl;
    private boolean nsfw;
    private List<TagDto> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }
}
