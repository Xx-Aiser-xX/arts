package org.example.arts.controllers;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.arts.dtos.*;
import org.example.arts.dtos.create.RegisterUserDto;
import org.example.arts.dtos.update.UserUpdateDto;
import org.example.arts.services.ArtService;
import org.example.arts.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ArtService artService;

    @Autowired
    public UserController(UserService userService, ArtService artService) {
        this.userService = userService;
        this.artService = artService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestBody RegisterUserDto request) {
        UserDto user = userService.registerUser(request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            @ModelAttribute UserUpdateDto request) throws FileUploadException {
        UserDto user = userService.updateUser(request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto user = userService.getCurrentUserDto();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/min")
    public ResponseEntity<UserMinDto> getCurrentMinUser() {
        UserMinDto user = userService.getCurrentUserMinDto();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/social-networks")
    public ResponseEntity<List<SocialNetworkDto>> getSocialNetworks(
            @RequestParam(required = false) String id) {
        List<SocialNetworkDto> user = userService.getSocialNetworkUser(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SubscribeDto> subscribe(
            @RequestParam(required = false) String id){
        SubscribeDto subDto = userService.subscribe(id);
        return ResponseEntity.ok(subDto);
    }

    @GetMapping("/subscribe")
    public ResponseEntity<Boolean> isSubscribe(
            @RequestParam(required = false) String id){
        boolean flag = userService.isSubscribe(id);
        return ResponseEntity.ok(flag);
    }

    @GetMapping()
    public ResponseEntity<UserDto> getUser(
            @RequestParam(required = false) String id) {
        UserDto user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/arts")
    public ResponseEntity<Page<ArtCardDto>> getArtByAuthor(@RequestParam(required = false) String id,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "12") Integer size){
        Page<ArtCardDto> arts = artService.getArtByAuthorId(id, page, size);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/subs-with-arts")
    public ResponseEntity<Page<UserMinDto>> getSubsWithArts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<UserMinDto> result = userService.getSubscriptionsWithArts(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-recent-sub")
    public ResponseEntity<List<UserMinDto>> getRecentSubscriptions(
            @RequestParam(defaultValue = "4", required = false) Integer limit) {
        List<UserMinDto> user = userService.getRecentSubscriptions(limit);
        return ResponseEntity.ok(user);
    }
}
