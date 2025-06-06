package org.example.arts.repo;

import org.example.arts.entities.Comment;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository{
    Optional<Comment> findById(UUID id);
    Comment create(Comment entity);
    List<Comment> getAll(boolean deleted);
    Page<Comment> getPageEntities(int page, int size, boolean deleted);
    Comment save(Comment entity);

    List<Comment> findByArtId(UUID id, boolean deleted);
}
