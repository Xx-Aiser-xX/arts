package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.Tag;
import org.example.arts.repo.TagRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class TagRepositoryImpl extends BaseRepository<Tag> implements TagRepository {
    @PersistenceContext
    protected EntityManager em;

    public TagRepositoryImpl() {
        super(Tag.class);
    }

    public List<Tag> findByArtId(UUID id, boolean deleted){
        return em.createQuery(
                        "SELECT t " +
                                "FROM Tag t " +
                                "JOIN t.artTags at " +
                                "WHERE at.art.id = :id " +
                                "AND t.deleted = :deleted", Tag.class)
                .setParameter("id", id)
                .setParameter("deleted", deleted)
                .getResultList();
    }

    public boolean tagExists(String name){
        try {
            Tag tag =  em.createQuery(
                            "SELECT t " +
                                    "FROM Tag t " +
                                    "WHERE t.name = :name ", Tag.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return tag != null;
        }
        catch (NoResultException e){
            return false;
        }
    }

    @Override
    public List<String> getNotExistsTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        List<String> existingNames = em.createQuery(
                        "SELECT t.name " +
                                "FROM Tag t " +
                                "WHERE t.name IN :names", String.class)
                .setParameter("names", tags)
                .getResultList();
        return tags.stream()
                .filter(tag -> !existingNames.contains(tag)).toList();
    }

    @Override
    public Set<Tag> getExistsTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        List<Tag> existingNames = em.createQuery(
                        "SELECT t " +
                                "FROM Tag t " +
                                "WHERE t.name IN :names", Tag.class)
                .setParameter("names", tags)
                .getResultList();
        return new HashSet<>(existingNames);
    }
}
