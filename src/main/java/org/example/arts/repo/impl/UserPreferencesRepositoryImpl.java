package org.example.arts.repo.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.arts.entities.UserPreferences;
import org.example.arts.repo.UserPreferencesRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserPreferencesRepositoryImpl extends BaseRepository<UserPreferences> implements UserPreferencesRepository {
    @PersistenceContext
    protected EntityManager em;

    public UserPreferencesRepositoryImpl() {
        super(UserPreferences.class);
    }
}
