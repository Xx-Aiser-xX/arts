package org.example.arts.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.arts.dtos.CommentDto;
import org.example.arts.dtos.create.CommentCreateDto;
import org.example.arts.entities.Art;
import org.example.arts.entities.Comment;
import org.example.arts.entities.User;
import org.example.arts.exceptions.AuthorizationException;
import org.example.arts.exceptions.DataDeletedException;
import org.example.arts.repo.ArtRepository;
import org.example.arts.repo.CommentRepository;
import org.example.arts.services.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@EnableCaching
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepo;
    private final ArtRepository artRepo;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;

    public CommentServiceImpl(CommentRepository commentRepo, ArtRepository artRepo, ModelMapper modelMapper, CurrentUserService currentUserService) {
        this.commentRepo = commentRepo;
        this.artRepo = artRepo;
        this.modelMapper = modelMapper;
        this.currentUserService = currentUserService;
    }

    public CommentDto getCommentById(String id){
        UUID uuid = UUID.fromString(id);
        Comment com = commentRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));
        if (com.isDeleted())
            throw new DataDeletedException("Коментарий " + com.getAuthor().getUserName() + "от " + com.getPublicationTime() + " удалён");
        return modelMapper.map(com, CommentDto.class);
    }

    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public CommentCreateDto create(CommentCreateDto createDto){
        Comment com = modelMapper.map(createDto, Comment.class);
        Art art = artRepo.findById(UUID.fromString(createDto.getArtId()))
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        com.setAuthor(user);
        com.setArt(art);
        com.setPublicationTime(LocalDateTime.now());
        com = commentRepo.create(com);
        return modelMapper.map(com, CommentCreateDto.class);
    }

    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public CommentDto save(CommentDto commentDto){
        UUID commentUuid = UUID.fromString(commentDto.getId());
        Comment com = commentRepo.findById(commentUuid)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));
        modelMapper.map(commentDto, com);
        commentRepo.save(com);
        return modelMapper.map(com, CommentDto.class);
    }

    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public void deleted(String commentId){
        UUID commentUuid = UUID.fromString(commentId);
        Comment com = commentRepo.findById(commentUuid)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизован"));
        if (!user.equals(com.getAuthor()))
            throw new AuthorizationException("Вы не являетесь владельцем Арта");
        com.setDeleted(true);
        commentRepo.save(com);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#id + ':' + #page + ':' + #size")
    public Page<CommentDto> getCommentByArtId(String id, Integer page, Integer size){
        UUID uuid = UUID.fromString(id);
        List<Comment> comments = commentRepo.findByArtId(uuid,false);
        comments = comments.stream()
                .sorted(Comparator.comparing(Comment::getPublicationTime).reversed())
                .toList();
        List<CommentDto> commentsDto = comments.stream().map(comment -> modelMapper.map(comment, CommentDto.class)).toList();
        commentsDto = commentsDto.subList((page - 1) * size,
                Math.min((page - 1) * size + size, commentsDto.size()));
        return new PageImpl<>(commentsDto, PageRequest.of(page - 1, size), comments.size());
    }
}

