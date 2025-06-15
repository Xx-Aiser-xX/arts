package org.example.arts.services;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.arts.dtos.ArtCardDto;
import org.example.arts.dtos.ArtDto;
import org.example.arts.dtos.create.ArtCreateDto;
import org.example.arts.dtos.update.ArtUpdateDto;
import org.springframework.data.domain.Page;

public interface ArtService  {
    ArtDto getArtById(String id);
    ArtCreateDto create(ArtCreateDto artDto) throws FileUploadException;
    ArtDto save(ArtUpdateDto artDto) throws FileUploadException;
    void deleted(String artId);
    ArtDto findArtAndAuthorById(String id);
    Page<ArtCardDto> getArtByAuthorId(String id, Integer page, Integer size);
    void viewArt(String artId);
    boolean likeArt(String artId);
    Page<ArtCardDto> getLikedArtsByCurrentUser(int page, int size);
    boolean isLikeArt(String artId);
    Page<ArtCardDto> getFeed(String type, int page, int size);
    Page<ArtCardDto> searchArtsByTagNameAndName(String query, int page, int size);
}
