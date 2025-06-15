package org.example.arts.services.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.example.arts.dtos.TagDto;
import org.example.arts.dtos.create.TagCreateDto;
import org.example.arts.entities.Art;
import org.example.arts.entities.ArtTag;
import org.example.arts.entities.Tag;
import org.example.arts.exceptions.DataDeletedException;
import org.example.arts.repo.ArtTagRepository;
import org.example.arts.repo.TagRepository;
import org.example.arts.services.TagService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@EnableCaching
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepo;
    private final ArtTagRepository artTagRepo;
    private final ModelMapper modelMapper;

    public TagServiceImpl(TagRepository tagRepo, ArtTagRepository artTagRepo, ModelMapper modelMapper) {
        this.tagRepo = tagRepo;
        this.artTagRepo = artTagRepo;
        this.modelMapper = modelMapper;
    }

    public TagDto getTagById(String id){
        UUID uuid = UUID.fromString(id);
        Tag tag = tagRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Тег не найден"));
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
        UUID uuid = UUID.fromString(tagDto.getId());
        Tag tag = tagRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Тег не найден"));
        modelMapper.map(tagDto, tag);
        tagRepo.save(tag);
        return modelMapper.map(tag, TagDto.class);
    }

    @Transactional
    public void deleted(String tagId){
        UUID uuid = UUID.fromString(tagId);
        Tag tag = tagRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Тег не найден"));
        tag.setDeleted(true);
        tagRepo.save(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", key = "#art.id.toString()")
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
        List<String> list = tagRepo.getNotExistsTags(tagsToAdd);
        for (TagDto td : tagDto) {
            if (tagsToAdd.contains(td.getName())) {
                Tag tag = modelMapper.map(td, Tag.class);
                if (list.contains(tag.getName())){
                     tag = tagRepo.create(tag);
                }
                ArtTag artTag = new ArtTag(art, tag);
                artTagRepo.create(artTag);
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

    @Cacheable(value = "tags", key = "#id")
    public List<TagDto> getTagsByArtId(String id){
        UUID uuid = UUID.fromString(id);
        List<Tag> tags = tagRepo.findByArtId(uuid, false);
        return tags.stream().map(tag -> modelMapper.map(tag, TagDto.class)).toList();
    }
}
