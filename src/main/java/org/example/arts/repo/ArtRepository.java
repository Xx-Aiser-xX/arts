package org.example.arts.repo;

import org.example.arts.entities.Art;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ArtRepository {
    Optional<Art> findById(UUID id);
    Art create(Art entity);
    List<Art> getAll(boolean deleted);
    Page<Art> getPageEntities(int page, int size, boolean deleted);
    Art save(Art entity);

    List<Art> findByAuthor(UUID id, boolean deleted);
    Optional<Art> findArtAndAuthorById(UUID id, boolean deleted);
    List<Art> findByTagIdsExcludingArtIds(Set<UUID> tagIds, boolean deleted);
    List<Art> findByAuthorIds(Set<UUID> authorIds, boolean deleted);
    List<Art> findRecent(LocalDateTime since, boolean deleted);
    List<Art> searchByName(String query, boolean deleted);
}
