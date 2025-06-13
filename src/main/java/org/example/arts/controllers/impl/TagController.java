package org.example.arts.controllers.impl;

import org.example.arts.dtos.ArtCardDto;
import org.example.arts.dtos.TagDto;
import org.example.arts.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/tags")
public class TagController {

    public final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/art")
    public ResponseEntity<List<TagDto>> getArtByAuthor(
            @RequestParam(required = false) String id){
        List<TagDto> arts = tagService.getTagsByArtId(id);
        return ResponseEntity.ok(arts);
    }
}
