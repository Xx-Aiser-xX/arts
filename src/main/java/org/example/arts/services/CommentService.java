package org.example.arts.services;

import org.example.arts.dtos.CommentDto;
import org.example.arts.dtos.create.CommentCreateDto;
import org.springframework.data.domain.Page;

public interface CommentService {
    CommentDto getCommentById(String id);
    CommentCreateDto create(CommentCreateDto createDto);
    CommentDto save(CommentDto commentDto);
    void deleted(String commentId);
    Page<CommentDto> getCommentByArtId(String id, Integer page, Integer size);
}
