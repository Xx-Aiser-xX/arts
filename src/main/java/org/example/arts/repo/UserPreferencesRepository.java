package org.example.arts.repo;

import org.example.arts.entities.UserPreferences;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesRepository {
    Optional<UserPreferences> findById(UUID id);
    UserPreferences create(UserPreferences entity) ;
    UserPreferences save(UserPreferences entity);
}
