package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.SocialNetwork;
import org.example.arts.repo.SocialNetworkRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SocialNetworkRepositoryImpl extends BaseRepository<SocialNetwork> implements SocialNetworkRepository {
    @PersistenceContext
    protected EntityManager em;

    public SocialNetworkRepositoryImpl() {
        super(SocialNetwork.class);
    }

    public List<SocialNetwork> findByUserAndDeletedFalse(UUID id, boolean deleted){
        return em.createQuery(
                        "SELECT s " +
                                "FROM SocialNetwork s " +
                                "WHERE s.user.id = :id " +
                                "AND s.deleted = :deleted ", SocialNetwork.class)
                .setParameter("id", id)
                .setParameter("deleted", deleted)
                .getResultList();
    }
}
