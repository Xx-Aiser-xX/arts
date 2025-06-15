package org.example.arts.controllers.impl;

import org.example.arts.dtos.CommentDto;
import org.example.arts.dtos.PageForm;
import org.example.arts.dtos.create.CommentCreateDto;
import org.example.arts.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/art")
    public ResponseEntity<Page<CommentDto>> getArtById(
            @RequestParam(required = false) String id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size){
        Page<CommentDto> comments = commentService.getCommentByArtId(id, page, size);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/art")
    public ResponseEntity<CommentCreateDto> create(
            @RequestBody CommentCreateDto dto){
        CommentCreateDto createDto = commentService.create(dto);
        return ResponseEntity.ok(createDto);
    }

    @DeleteMapping
    public void deleted(@RequestParam String id){
        commentService.deleted(id);
    }
}
