package org.example.arts.services;

import org.example.arts.dtos.TagDto;
import org.example.arts.dtos.create.TagCreateDto;
import org.example.arts.entities.Art;
import java.util.List;

public interface TagService {
    TagDto getTagById(String id);
    TagCreateDto create(TagCreateDto tagDto);
    TagDto save(TagDto tagDto);
    void deleted(String tagId);
    void updateListTagsInArt(List<TagDto> tagDto, Art art);
    List<TagDto> getTagsByArtId(String id);
}
