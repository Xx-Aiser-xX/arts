package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public abstract class BaseRepository<Entity> {
    private final Class<Entity> entityClass;
    @PersistenceContext
    protected EntityManager em;

    @Autowired
    public BaseRepository(Class<Entity> entityClass) {
        this.entityClass = entityClass;
    }

    public Entity create(Entity entity) {
        try {
            em.persist(entity);
            em.flush();
            return entity;
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Entity> getAll(boolean deleted) {
        return em.createQuery("FROM " + entityClass.getName() + " e " +
                        "WHERE e.deleted = :isDeleted", entityClass)
                .setParameter("isDeleted", deleted)
                .getResultList();
    }

    public Page<Entity> getPageEntities(int page, int size, boolean deleted) {
        try {
            long total = em.createQuery(
                    "SELECT COUNT(e) FROM " + entityClass.getName() + " e " +
                            "WHERE e.deleted = :isDeleted", Long.class)
                    .setParameter("isDeleted", deleted)
                    .getSingleResult();

            List<Entity> entities = em.createQuery(
                    "FROM " + entityClass.getName() + " e " +
                            "WHERE e.deleted = :isDeleted", entityClass)
                    .setParameter("isDeleted", deleted)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();

            return new PageImpl<>(entities, PageRequest.of(page, size), total);
        } catch (NoResultException e) {
            return Page.empty();
        }
    }

    public Optional<Entity> findById(UUID id) {
        try {
            return Optional.of(em.find(entityClass, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Entity save(Entity entity) {
        try {
            return em.merge(entity);
        } catch (Exception e) {
            return null;
        }
    }
}
