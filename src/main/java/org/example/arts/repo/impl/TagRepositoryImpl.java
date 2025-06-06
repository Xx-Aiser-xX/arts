package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.Tag;
import org.example.arts.repo.TagRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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
}
