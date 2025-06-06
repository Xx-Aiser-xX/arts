package org.example.arts.repo;

import org.example.arts.entities.Interaction;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InteractionRepository {
    Optional<Interaction> findById(UUID id);
    Interaction create(Interaction entity);
    List<Interaction> getAll(boolean deleted);
    Page<Interaction> getPageEntities(int page, int size, boolean deleted);
    Interaction save(Interaction entity);

    Interaction findByArtIdAndUserId(UUID artId, UUID userId, boolean deleted);
    List<Interaction> findLikedByUser(UUID userId, boolean isLike, boolean deleted);
    List<Interaction> findWithArtTagsByUserId(UUID userId, boolean deleted);
    List<Interaction> findRecentLikes(LocalDateTime since, boolean like, boolean deleted);
}
