package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.Art;
import org.example.arts.repo.ArtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    public Art findArtAndAuthorById(UUID id, boolean deleted){
        return em.createQuery(
                        "SELECT a " +
                                "FROM Art a " +
                                "JOIN FETCH a.author u " +
                                "WHERE a.id = :id " +
                                "AND a.deleted = :deleted", Art.class)
                .setParameter("id", id)
                .setParameter("deleted", deleted)
                .getSingleResult();
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
