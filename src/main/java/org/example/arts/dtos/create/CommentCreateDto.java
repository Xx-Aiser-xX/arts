package org.example.arts.dtos.create;

public class CommentCreateDto {
    private String artId;
    private String text;

    public String getArtId() {
        return artId;
    }

    public void setArtId(String artId) {
        this.artId = artId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
