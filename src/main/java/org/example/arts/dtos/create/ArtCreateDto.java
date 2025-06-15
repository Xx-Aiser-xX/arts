package org.example.arts.dtos.create;

import org.example.arts.dtos.TagDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

public class ArtCreateDto implements Serializable {
    private String name;
    private String description;
    private MultipartFile imageFile;
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

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
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
