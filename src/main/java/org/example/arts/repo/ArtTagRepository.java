package org.example.arts.repo;

import org.example.arts.entities.Art;
import org.example.arts.entities.ArtTag;
import org.example.arts.entities.Tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArtTagRepository {
    ArtTag create(ArtTag entity);
    ArtTag save(ArtTag entity);

    Optional<ArtTag> findByTagIdAndArtId(UUID tagId, UUID artId);
    List<ArtTag> findByArtId(UUID artId, boolean isDeleted);
}
