package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.Interaction;
import org.example.arts.repo.InteractionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InteractionRepositoryImpl extends BaseRepository<Interaction> implements InteractionRepository {
    @PersistenceContext
    protected EntityManager em;

    public InteractionRepositoryImpl() {
        super(Interaction.class);
    }

    public Optional<Interaction> findByArtIdAndUserId(UUID artId, UUID userId, boolean deleted){
        try {
            return Optional.ofNullable(em.createQuery(
                            "SELECT i " +
                                    "FROM Interaction i " +
                                    "WHERE i.user.id = :userId " +
                                    "AND i.art.id = :artId " +
                                    "AND i.deleted = :deleted ", Interaction.class)
                    .setParameter("userId", userId)
                    .setParameter("artId", artId)
                    .setParameter("deleted", deleted)
                    .getSingleResult());
        }
        catch (NoResultException e){
            return Optional.empty();
        }
    }

    public List<Interaction> findLikedByUser(UUID userId, boolean isLike, boolean deleted) {
        return em.createQuery(
                        "SELECT i " +
                                "FROM Interaction i " +
                                "WHERE i.user.id = :userId " +
                                "AND i.like = :isLike " +
                                "AND i.deleted = :deleted", Interaction.class)
                .setParameter("userId", userId)
                .setParameter("deleted", deleted)
                .setParameter("isLike", isLike)
                .getResultList();
    }

    public List<Interaction> findWithArtTagsByUserId(UUID userId, boolean deleted) {
        return em.createQuery(
                        "SELECT i FROM Interaction i " +
                                "JOIN FETCH i.art a " +
                                "JOIN FETCH a.artTagSet at " +
                                "JOIN FETCH at.tag " +
                                "WHERE i.user.id = :userId " +
                                "AND i.deleted = :deleted", Interaction.class)
                .setParameter("userId", userId)
                .setParameter("deleted", deleted)
                .getResultList();
    }

    public List<Interaction> findRecentLikes(LocalDateTime since, boolean like, boolean deleted) {
        return em.createQuery(
                        "SELECT i FROM Interaction i " +
                                "JOIN FETCH i.art a " +
                                "WHERE i.like = :like " +
                                "AND i.deleted = :deleted " +
                                "AND i.likedAt >= :since " +
                                "AND a.deleted = :deleted", Interaction.class)
                .setParameter("since", since)
                .setParameter("like", like)
                .setParameter("deleted", deleted)
                .getResultList();
    }

}
