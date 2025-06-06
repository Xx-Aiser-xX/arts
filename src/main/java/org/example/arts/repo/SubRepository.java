package org.example.arts.repo;

import org.example.arts.entities.Sub;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubRepository {
    Optional<Sub> findById(UUID id);
    Sub create(Sub entity) ;
    List<Sub> getAll(boolean deleted);
    Page<Sub> getPageEntities(int page, int size, boolean deleted);
    Sub save(Sub entity);

    Sub signed(UUID idUser, UUID idAuthor);
    List<Sub> findBySubscriberId(UUID subscriberId, boolean deleted);
    List<Sub> findSubAndUserBySubscriberId(UUID subscriberId, boolean deleted);
    List<Sub> findBySubscriber(UUID subscriberId, boolean deleted);
}
