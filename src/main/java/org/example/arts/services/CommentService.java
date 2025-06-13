package org.example.arts.services;

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
import org.example.arts.repo.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepo;
    private final UserRepository userRepo;
    private final ArtRepository artRepo;
    private final ModelMapper modelMapper;

    public CommentService(CommentRepository commentRepo, UserRepository userRepo, ArtRepository artRepo, ModelMapper modelMapper) {
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.artRepo = artRepo;
        this.modelMapper = modelMapper;
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
    public CommentCreateDto create(CommentCreateDto createDto){
        Comment com = modelMapper.map(createDto, Comment.class);
        Art art = artRepo.findById(UUID.fromString(createDto.getArtId()))
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
        User user = getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        com.setAuthor(user);
        com.setArt(art);
        com.setPublicationTime(LocalDateTime.now());
        com = commentRepo.create(com);
        return modelMapper.map(com, CommentCreateDto.class);
    }

    @Transactional
    public CommentDto save(CommentDto commentDto){
        UUID commentUuid = UUID.fromString(commentDto.getId());
        Comment com = commentRepo.findById(commentUuid)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));
        modelMapper.map(commentDto, com);
        commentRepo.save(com);
        return modelMapper.map(com, CommentDto.class);
    }

    @Transactional
    public void deleted(String commentId){
        UUID commentUuid = UUID.fromString(commentId);
        Comment com = commentRepo.findById(commentUuid)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));
        com.setDeleted(true);
        commentRepo.save(com);
    }

    @Transactional(readOnly = true)
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

    private Jwt getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }

    private Optional<UUID> getCurrentUserId(){
        Jwt jwt = getJwt();
        if (jwt == null)
            return Optional.empty();
        return Optional.of(UUID.fromString(jwt.getSubject()));
    }

    private Optional<User> getCurrentUser(){
        Jwt jwt = getJwt();
        if (jwt == null)
            return Optional.empty();
        UUID id = UUID.fromString(jwt.getSubject());
        return userRepo.findById(id);
    }
}

