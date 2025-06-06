package org.example.arts.repo;

import org.example.arts.entities.ArtTag;

public interface ArtTagRepository {
    ArtTag create(ArtTag entity);
    ArtTag save(ArtTag entity);

}
