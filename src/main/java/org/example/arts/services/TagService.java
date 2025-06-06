package org.example.arts.services;

import jakarta.transaction.Transactional;
import org.example.arts.dtos.ArtDto;
import org.example.arts.dtos.CommentDto;
import org.example.arts.dtos.TagDto;
import org.example.arts.dtos.create.CommentCreateDto;
import org.example.arts.dtos.create.TagCreateDto;
import org.example.arts.entities.Art;
import org.example.arts.entities.ArtTag;
import org.example.arts.entities.Comment;
import org.example.arts.entities.Tag;
import org.example.arts.exceptions.DataDeletedException;
import org.example.arts.repo.ArtTagRepository;
import org.example.arts.repo.TagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepo;
    private final ArtTagRepository artTagRepo;
    private final ModelMapper modelMapper;

    public TagService(TagRepository tagRepo, ArtTagRepository artTagRepo, ModelMapper modelMapper) {
        this.tagRepo = tagRepo;
        this.artTagRepo = artTagRepo;
        this.modelMapper = modelMapper;
    }

    public TagDto getCommentById(String id){
        UUID uuid = UUID.fromString(id);
        Tag tag = tagRepo.findById(uuid).get();
        if (tag.isDeleted())
            throw new DataDeletedException("Тег " + tag.getName() + " удалён");
        return modelMapper.map(tag, TagDto.class);
    }

    @Transactional
    public TagCreateDto create(TagCreateDto tagDto){
        Tag tag = modelMapper.map(tagDto, Tag.class);
        tagRepo.create(tag);
        return modelMapper.map(tag, TagCreateDto.class);
    }

    @Transactional
    public TagDto save(TagDto tagDto){
        Tag tag = modelMapper.map(tagDto, Tag.class);
        tagRepo.save(tag);
        return modelMapper.map(tag, TagDto.class);
    }

    @Transactional
    public void deleted(TagDto tagDto){
        Tag tag = modelMapper.map(tagDto, Tag.class);
        tag.setDeleted(true);
        tagRepo.save(tag);
    }

    public void updateListTagsInArt(List<TagDto> tagDto, Art art){

        List<Tag> tags = tagRepo.findByArtId(art.getId(), false);
        Set<String> newTagNames = tagDto.stream()
                .map(TagDto::getName)
                .collect(Collectors.toSet());
        Set<String> existingTagNames = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<String> tagsToAdd = new HashSet<>(newTagNames);
        tagsToAdd.removeAll(existingTagNames);

        for (TagDto td : tagDto) {
            if (tagsToAdd.contains(td.getName())) {
                Tag tag = modelMapper.map(td, Tag.class);
                ArtTag artTag = new ArtTag(art, tag);
                artTagRepo.save(artTag);
            }
        }

        Set<String> tagsToRemove = new HashSet<>(existingTagNames);
        tagsToRemove.removeAll(newTagNames);

        for (Tag tag : tags) {
            if (tagsToRemove.contains(tag.getName())) {
                ArtTag artTag = new ArtTag(art, tag);
                artTag.setDeleted(true);
                artTagRepo.save(artTag);
            }
        }
    }

    public List<TagDto> getTagsByArtId(String id){
        UUID uuid = UUID.fromString(id);
        List<Tag> tags = tagRepo.findByArtId(uuid, false);
        return tags.stream().map(tag -> modelMapper.map(tag, TagDto.class)).toList();
    }
}
