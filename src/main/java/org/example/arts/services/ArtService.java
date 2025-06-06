package org.example.arts.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.arts.entities.*;
import org.example.arts.repo.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.example.arts.dtos.*;
import org.example.arts.dtos.create.ArtCreateDto;
import org.example.arts.exceptions.DataDeletedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArtService {
    private final ArtRepository artRepo;
    private final UserRepository userRepo;
    private final TagRepository tagRepo;
    private final ArtTagRepository artTagRepo;
    private final InteractionRepository interactionRepo;
    private final SubRepository subRepo;
    private final TagService tagService;
    private final ModelMapper modelMapper;

    @Autowired
    public ArtService(ArtRepository artRepo, UserRepository userRepo, TagRepository tagRepo, ArtTagRepository artTagRepo, InteractionRepository interactionRepo, SubRepository subRepo, TagService tagService, ModelMapper modelMapper) {
        this.artRepo = artRepo;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.artTagRepo = artTagRepo;
        this.interactionRepo = interactionRepo;
        this.subRepo = subRepo;
        this.tagService = tagService;
        this.modelMapper = modelMapper;
    }

    public ArtDto getArtById(String id){
        UUID uuid = UUID.fromString(id);
        Art art = artRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
        if (art.isDeleted())
            throw new DataDeletedException("Удалён: " + art.getName());
        return modelMapper.map(art, ArtDto.class);
    }

    @Transactional
    public ArtCreateDto create(ArtCreateDto artDto){
        Art art = modelMapper.map(artDto, Art.class);
        art.setAuthor(getCurrentUser());
        art.setPublicationTime(LocalDateTime.now());
        Set<Tag> tags = new HashSet<>();
        if(!artDto.getTags().isEmpty()){
            for (TagDto tag : artDto.getTags()){
                if (!tagRepo.tagExists(tag.getName())){
                    tags.add(modelMapper.map(tag, Tag.class));
                }
            }
        }
        art = artRepo.create(art);
        for (Tag tag : tags){
            tag = tagRepo.create(tag);
            ArtTag artTag = new ArtTag(art, tag);
            artTagRepo.create(artTag);
        }
        return modelMapper.map(art, ArtCreateDto.class);
    }

    @Transactional
    public ArtDto save(ArtDto artDto, List<TagDto> tagDto) {
        Art art = modelMapper.map(artDto, Art.class);
        artRepo.save(art);
        tagService.updateListTagsInArt(tagDto, art);
        return modelMapper.map(art, ArtDto.class);
    }


    @Transactional
    public void deleted(ArtDto artDto){
        Art art = modelMapper.map(artDto, Art.class);
        art.setDeleted(true);
        artRepo.save(art);
    }

    @Transactional(readOnly = true)
    public ArtDto findArtAndAuthorById(String id){
        UUID uuid = UUID.fromString(id);
        Art art = artRepo.findArtAndAuthorById(uuid, false);
        ArtDto artDto = modelMapper.map(art, ArtDto.class);
//        artDto.setAuthor(modelMapper.map(art.getAuthor(), UserDto.class));
        return artDto;
    }

    public Page<ArtCardDto> getArtByAuthorId(String id, Integer page, Integer size){
        UUID uuid = UUID.fromString(id);
        List<Art> arts = artRepo.findByAuthor(uuid, false);
        List<ArtCardDto> artCards = arts.stream().map(art -> modelMapper.map(art, ArtCardDto.class)).toList();
        artCards = artCards.stream()
                .sorted(Comparator.comparing(ArtCardDto::getPublicationTime).reversed())
                .toList();
        artCards = artCards.subList((page - 1) * size, Math.min(
                        (page - 1) * size + size, artCards.size()));
        return new PageImpl<>(artCards, PageRequest.of(page - 1, size), arts.size());
    }

    @Transactional
    public void viewArt(String artId){
        UUID uuid = UUID.fromString(artId);
        Art art = artRepo.findById(uuid).get();
        User user = getCurrentUser();
        Interaction interaction = interactionRepo.findByArtIdAndUserId(uuid, user.getId(), false);
        if (interaction == null){
            interactionRepo.create(new Interaction(user,art));
            art.setCountViews(art.getCountViews() + 1);
            artRepo.save(art);
        }
    }

    @Transactional
    public boolean likeArt(String artId){
        UUID uuid = UUID.fromString(artId);
        Art art = artRepo.findById(uuid).get();
        User user = getCurrentUser();
        Interaction interaction = interactionRepo.findByArtIdAndUserId(uuid, user.getId(), false);
        if (!interaction.isLike()) {
            interaction.setLike(true);
            interaction.setLikedAt(LocalDateTime.now());
            interactionRepo.save(interaction);
            art.setCountLikes(art.getCountLikes() + 1);
            artRepo.save(art);
            return true;
        }
        else if (interaction.getLikedAt() != null){
            interaction.setLike(false);
            interactionRepo.save(interaction);
            art.setCountLikes(art.getCountLikes() - 1);
            artRepo.save(art);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Page<ArtCardDto> getLikedArtsByCurrentUser(int page, int size) {
        UUID userId = getCurrentUserId();
        List<Interaction> likes = interactionRepo.findLikedByUser(userId, true, false);

        List<ArtCardDto> likedArts = likes.stream()
                .map(Interaction::getArt)
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        List<ArtCardDto> paged = likedArts.subList((page - 1) * size, Math.min((page - 1) * size + size, likedArts.size()));

        return new PageImpl<>(paged, PageRequest.of(page - 1, size), likedArts.size());
    }


    public boolean isLikeArt(String artId){
        UUID uuid = UUID.fromString(artId);
        User user = getCurrentUser();
        Interaction interaction = interactionRepo.findByArtIdAndUserId(uuid, user.getId(), false);
        return interaction.isLike();
    }

    public Page<ArtCardDto> getFeed(String type, int page, int size) {
        return switch (type) {
            case "trending" -> getTrendingArts(page, size);
            case "recommended" -> getRecommendedArts(page, size);
            case "subscriptions" -> getSubscribedArts(page, size);
            case "latest" -> getLatestArts(page, size);
            default -> throw new IllegalArgumentException("Invalid feed type");
        };
    }

    public Page<ArtCardDto> searchArtsByName(String query, int page, int size) {
        List<Art> arts = artRepo.searchByName(query, false);

        List<ArtCardDto> sorted = arts.stream()
                .sorted(Comparator.comparing(Art::getPublicationTime).reversed())
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        List<ArtCardDto> paged = sorted.subList((page - 1) * size,
                Math.min((page - 1) * size + size, sorted.size()));

        return new PageImpl<>(paged, PageRequest.of(page - 1, size), sorted.size());
    }



    private Page<ArtCardDto> getTrendingArts(int page, int size) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<Interaction> likes = interactionRepo.findRecentLikes(cutoff, true, false);

        Map<Art, Long> likeCount = likes.stream()
                .collect(Collectors.groupingBy(Interaction::getArt, Collectors.counting()));

        List<ArtCardDto> trending = likeCount.entrySet().stream()
                .sorted(Map.Entry.<Art, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        return paginate(trending, page, size);
    }


    private Page<ArtCardDto> getRecommendedArts(int page, int size) {
        UUID userId = getCurrentUserId();

        List<Interaction> interactions = interactionRepo.findWithArtTagsByUserId(userId, false);

        Set<UUID> tagIds = interactions.stream()
                .flatMap(i -> i.getArt().getArtTagSet().stream())
                .map(artTag -> artTag.getTag().getId())
                .collect(Collectors.toSet());

        List<Art> candidateArts = artRepo.findByTagIdsExcludingArtIds(tagIds, false);

        List<ArtCardDto> recommended = candidateArts.stream()
                .sorted(Comparator.comparing(Art::getPublicationTime).reversed())
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        return paginate(recommended, page, size);
    }

    private Page<ArtCardDto> getSubscribedArts(int page, int size) {
        UUID userId = getCurrentUserId();
        List<Sub> subs = subRepo.findSubAndUserBySubscriberId(userId, false);

        Set<UUID> authorIds = subs.stream()
                .map(sub -> sub.getTarget().getId())
                .collect(Collectors.toSet());

        List<Art> arts = artRepo.findByAuthorIds(authorIds, false);

        List<ArtCardDto> dtos = arts.stream()
                .sorted(Comparator.comparing(Art::getPublicationTime).reversed())
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        return paginate(dtos, page, size);
    }


    private Page<ArtCardDto> getLatestArts(int page, int size) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Art> latest = artRepo.findRecent(oneWeekAgo, false);

        List<ArtCardDto> result = latest.stream()
                .sorted(Comparator.comparing(Art::getPublicationTime).reversed())
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        return paginate(result, page, size);
    }

    private Page<ArtCardDto> paginate(List<ArtCardDto> list, int page, int size) {
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, list.size());
        if (fromIndex >= list.size()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), list.size());
        }
        return new PageImpl<>(list.subList(fromIndex, toIndex), PageRequest.of(page - 1, size), list.size());
    }


    private Jwt getJwt(){
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private UUID getCurrentUserId(){
        Jwt jwt = getJwt();
        return UUID.fromString(jwt.getSubject());
    }

    private User getCurrentUser(){
        UUID id = UUID.fromString(getJwt().getSubject());
        Optional<User> user = userRepo.findById(id);
        return user.get();
    }
}
