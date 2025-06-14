package org.example.arts.controllers;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.arts.dtos.ArtCardDto;
import org.example.arts.dtos.ArtDto;
import org.example.arts.dtos.create.ArtCreateDto;
import org.example.arts.dtos.update.ArtUpdateDto;
import org.example.arts.services.ArtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/arts")
public class ArtController {

    private final ArtService artService;

    @Autowired
    public ArtController(ArtService artService) {
        this.artService = artService;
    }

    @GetMapping("/with-author")
    public ResponseEntity<ArtDto> findArtAndAuthorById(
            @RequestParam(required = false) String id){
        ArtDto art = artService.findArtAndAuthorById(id);
        artService.viewArt(id);
        return ResponseEntity.ok(art);
    }

    @PostMapping("/like")
    public ResponseEntity<Boolean> likeArt(
            @RequestParam(required = false) String id){
        boolean like = artService.likeArt(id);
        return ResponseEntity.ok(like);
    }

    @GetMapping("/like")
    public ResponseEntity<Boolean> isLikeArt(
            @RequestParam(required = false) String id){
        boolean like = artService.isLikeArt(id);
        return ResponseEntity.ok(like);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtCreateDto> createArt(@ModelAttribute ArtCreateDto artCreateDto) throws FileUploadException {
        ArtCreateDto createdArt = artService.create(artCreateDto);
        return ResponseEntity.ok(createdArt);
    }

    @DeleteMapping
    public void deletedArt(@RequestParam String id){
        artService.deleted(id);
    }

    @PutMapping()
    public ResponseEntity<ArtDto> updateArt(@ModelAttribute ArtUpdateDto artDto) throws FileUploadException {
        ArtDto art = artService.save(artDto);
        return ResponseEntity.ok(art);
    }

    @GetMapping("/likes")
    public ResponseEntity<Page<ArtCardDto>> getLikedArts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<ArtCardDto> likedArts = artService.getLikedArtsByCurrentUser(page, size);
        return ResponseEntity.ok(likedArts);
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<ArtCardDto>> getFeed(
            @RequestParam(defaultValue = "latest") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<ArtCardDto> feed = artService.getFeed(type, page, size);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ArtCardDto>> searchArts(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<ArtCardDto> results = artService.searchArtsByTagNameAndName(query, page, size);
        return ResponseEntity.ok(results);
    }
}
