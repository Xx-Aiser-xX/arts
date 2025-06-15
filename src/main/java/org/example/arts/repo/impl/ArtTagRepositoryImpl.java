package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.ArtTag;
import org.example.arts.repo.ArtTagRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ArtTagRepositoryImpl extends BaseRepository<ArtTag> implements ArtTagRepository {
    @PersistenceContext
    protected EntityManager em;

    public ArtTagRepositoryImpl() {
        super(ArtTag.class);
    }

    @Override
    public Optional<ArtTag> findByTagIdAndArtId(UUID tagId, UUID artId) {
        return Optional.ofNullable((ArtTag) em.createQuery(
                "SELECT at FROM ArtTag at " +
                        "WHERE at.art.id = :artId " +
                        "AND at.tag.id = :tagId", ArtTag.class)
                .setParameter("artId", artId)
                .setParameter("tagId", tagId)
                .getResultList());
    }

    @Override
    public List<ArtTag> findByArtId(UUID artId, boolean isDeleted) {
        return em.createQuery(
                "SELECT at FROM ArtTag at " +
                        "WHERE at.art.id = :artId " +
                        "AND at.deleted = :isDeleted", ArtTag.class)
                .setParameter("artId",artId)
                .setParameter("isDeleted",isDeleted)
                .getResultList();
    }


}
