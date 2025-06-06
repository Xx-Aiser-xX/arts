package org.example.arts.repo;

import org.example.arts.entities.SocialNetwork;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SocialNetworkRepository{
    Optional<SocialNetwork> findById(UUID id);
    SocialNetwork create(SocialNetwork entity);
    List<SocialNetwork> getAll(boolean deleted);
    Page<SocialNetwork> getPageEntities(int page, int size, boolean deleted);
    SocialNetwork save(SocialNetwork entity);

    List<SocialNetwork> findByUserAndDeletedFalse(UUID id, boolean deleted);
}