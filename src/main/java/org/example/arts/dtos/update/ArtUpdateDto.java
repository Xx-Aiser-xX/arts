package org.example.arts.dtos.update;

import org.example.arts.dtos.TagDto;
import org.example.arts.dtos.UserMinDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ArtUpdateDto {
    private UUID id;
    private String name;
    private String description;
    private MultipartFile imageFile;
    private boolean nsfw;
    private List<TagDto> tags;
    private UserMinDto author;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public UserMinDto getAuthor() {
        return author;
    }

    public void setAuthor(UserMinDto author) {
        this.author = author;
    }
}
