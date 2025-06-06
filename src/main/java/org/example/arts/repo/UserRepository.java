package org.example.arts.repo;

import org.example.arts.entities.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    User create(User entity) ;
    List<User> getAll(boolean deleted);
    Page<User> getPageEntities(int page, int size, boolean deleted);
    User save(User entity);

    Optional<User> findByUserName(String userName, boolean deleted);
}
