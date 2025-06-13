package org.example.arts.repo;

import org.example.arts.entities.Tag;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository {
    Optional<Tag> findById(UUID id);
    Tag create(Tag entity) ;
    List<Tag> getAll(boolean deleted);
    Page<Tag> getPageEntities(int page, int size, boolean deleted);
    Tag save(Tag entity);

    List<Tag> findByArtId(UUID artId, boolean deleted);
    boolean tagExists(String name);
    List<String> getNotExistsTags(Set<String> tags);
//    Set<Tag> getSetNotExistsTags(Set<String> tags);
}
