package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.Comment;
import org.example.arts.repo.CommentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CommentRepositoryImpl extends BaseRepository<Comment> implements CommentRepository {
    @PersistenceContext
    protected EntityManager em;

    public CommentRepositoryImpl() {
        super(Comment.class);
    }

    public List<Comment> findByArtId(UUID id, boolean deleted){
        return em.createQuery(
                "SELECT c " +
                        "FROM Comment c " +
                        "WHERE c.art.id = :id " +
                        "AND c.deleted = :deleted ", Comment.class)
                .setParameter("id", id)
                .setParameter("deleted", deleted)
                .getResultList();
    }
}
