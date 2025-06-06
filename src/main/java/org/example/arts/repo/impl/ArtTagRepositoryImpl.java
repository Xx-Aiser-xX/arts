package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.ArtTag;
import org.example.arts.repo.ArtTagRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ArtTagRepositoryImpl extends BaseRepository<ArtTag> implements ArtTagRepository {
    @PersistenceContext
    protected EntityManager em;

    public ArtTagRepositoryImpl() {
        super(ArtTag.class);
    }
}
