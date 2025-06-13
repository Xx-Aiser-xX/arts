package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.Sub;
import org.example.arts.repo.SubRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SubRepositoryImpl extends BaseRepository<Sub> implements SubRepository {
    @PersistenceContext
    protected EntityManager em;

    public SubRepositoryImpl() {
        super(Sub.class);
    }

    public Optional<Sub> signed(UUID idUser, UUID idAuthor){
        try {
            return Optional.ofNullable(em.createQuery(
                            "SELECT s " +
                                    "FROM Sub s " +
                                    "WHERE s.subscriber.id =:idUser " +
                                    "AND s.target.id = :idAuthor", Sub.class)
                    .setParameter("idUser", idUser)
                    .setParameter("idAuthor", idAuthor)
                    .getSingleResult());
        }
        catch (NoResultException e){
            return Optional.empty();
        }
    }

    public List<Sub> findBySubscriberId(UUID subscriberId, boolean deleted) {
        return em.createQuery(
                "SELECT s " +
                        "FROM Sub s " +
                        "WHERE s.subscriber.id = :subscriberId " +
                        "AND s.deleted = :deleted", Sub.class)
                .setParameter("subscriberId", subscriberId)
                .setParameter("deleted", deleted)
                .getResultList();

    }

    public List<Sub> findSubAndUserBySubscriberId(UUID subscriberId, boolean deleted) {
        return em.createQuery(
                "SELECT s " +
                        "FROM Sub s " +
                        "JOIN FETCH s.target t " +
                        "WHERE s.subscriber.id = :subscriberId " +
                        "AND s.deleted = :deleted", Sub.class)
                .setParameter("subscriberId", subscriberId)
                .setParameter("deleted", deleted)
                .getResultList();
    }

    public List<Sub> findBySubscriber(UUID subscriberId, boolean deleted) {
        return em.createQuery(
                        "SELECT s FROM Sub s " +
                                "JOIN FETCH s.target " +
                                "WHERE s.subscriber.id = :subscriberId " +
                                "AND s.deleted = :deleted", Sub.class)
                .setParameter("subscriberId", subscriberId)
                .setParameter("deleted", deleted)
                .getResultList();
    }
}
