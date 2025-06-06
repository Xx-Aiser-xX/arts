package org.example.arts.dtos;

import java.util.List;

public class SubWithArtsDto {
    private UserMinDto author;
    private List<ArtCardDto> arts;

    public UserMinDto getAuthor() {
        return author;
    }

    public void setAuthor(UserMinDto author) {
        this.author = author;
    }

    public List<ArtCardDto> getArts() {
        return arts;
    }

    public void setArts(List<ArtCardDto> arts) {
        this.arts = arts;
    }
}
