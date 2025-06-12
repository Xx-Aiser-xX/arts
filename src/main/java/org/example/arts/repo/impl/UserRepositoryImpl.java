package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.User;
import org.example.arts.repo.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl  extends BaseRepository<User> implements UserRepository {
    @PersistenceContext
    protected EntityManager em;

    public UserRepositoryImpl() {
        super(User.class);
    }

    public Optional<User> findByUserName(String userName, boolean deleted){
        try {
            return Optional.ofNullable(em.createQuery(
                            "SELECT u " +
                                    "FROM User u " +
                                    "WHERE u.userName = :userName " +
                                    "AND u.deleted = :deleted ", User.class)
                    .setParameter("userName", userName)
                    .setParameter("deleted", deleted)
                    .getSingleResult());
        }
        catch (NoResultException e){
            return Optional.empty();
        }
    }
}