package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.Art;
import org.example.arts.repo.ArtRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class ArtRepositoryImpl extends BaseRepository<Art> implements ArtRepository {

    @PersistenceContext
    protected EntityManager em;

    public ArtRepositoryImpl() {
        super(Art.class);
    }

    @Override
    public List<Art> findByAuthor(UUID id, boolean deleted){
        return em.createQuery(
                        "SELECT a " +
                                "FROM Art a " +
                                "JOIN a.author u " +
                                "WHERE u.id = :id " +
                                "AND a.deleted = :deleted", Art.class)
                .setParameter("id", id)
                .setParameter("deleted", deleted)
                .getResultList();
    }

    @Override
    public Optional<Art> findArtAndAuthorById(UUID id, boolean deleted){
        try {
            return Optional.ofNullable(em.createQuery(
                            "SELECT a " +
                                    "FROM Art a " +
                                    "JOIN FETCH a.author u " +
                                    "WHERE a.id = :id " +
                                    "AND a.deleted = :deleted", Art.class)
                    .setParameter("id", id)
                    .setParameter("deleted", deleted)
                    .getSingleResult());
        }
        catch (NoResultException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Art> findByTagIdsExcludingArtIds(Set<UUID> tagIds, boolean deleted) {
        return em.createQuery(
                        "SELECT DISTINCT a FROM Art a " +
                                "JOIN a.artTagSet at " +
                                "WHERE at.tag.id IN :tagIds " +
                                "AND a.deleted = :deleted", Art.class)
                .setParameter("tagIds", tagIds)
                .setParameter("deleted", deleted)
                .getResultList();
    }

    public List<Art> findByAuthorIds(Set<UUID> authorIds, boolean deleted) {
        return em.createQuery(
                        "SELECT DISTINCT a FROM Art a " +
                                "JOIN FETCH a.author " +
                                "WHERE a.author.id IN :authorIds " +
                                "AND a.deleted = :deleted", Art.class)
                .setParameter("authorIds", authorIds)
                .setParameter("deleted", deleted)
                .getResultList();
    }

    public List<Art> findRecent(LocalDateTime since, boolean deleted) {
        return em.createQuery(
                        "SELECT DISTINCT a FROM Art a " +
                                "JOIN FETCH a.author " +
                                "WHERE a.deleted = :deleted AND a.publicationTime >= :since", Art.class)
                .setParameter("deleted", deleted)
                .setParameter("since", since)
                .getResultList();
    }

    public List<Art> searchByName(String query, boolean deleted) {
        return em.createQuery(
                        "SELECT a FROM Art a " +
                                "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                                "AND a.deleted = :deleted", Art.class)
                .setParameter("query", query)
                .setParameter("deleted", deleted)
                .getResultList();
    }


}
