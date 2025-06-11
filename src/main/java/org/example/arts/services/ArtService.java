package org.example.arts.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.arts.dtos.update.ArtUpdateDto;
import org.example.arts.entities.*;
import org.example.arts.exceptions.AuthorizationException;
import org.example.arts.repo.*;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
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
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    public ArtService(ArtRepository artRepo, UserRepository userRepo, TagRepository tagRepo, ArtTagRepository artTagRepo, InteractionRepository interactionRepo, SubRepository subRepo, TagService tagService, ModelMapper modelMapper, S3Client s3Client) {
        this.artRepo = artRepo;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.artTagRepo = artTagRepo;
        this.interactionRepo = interactionRepo;
        this.subRepo = subRepo;
        this.tagService = tagService;
        this.modelMapper = modelMapper;
        this.s3Client = s3Client;
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
        Optional<User> user = getCurrentUser();
        if (user.isEmpty())
            throw new AuthorizationException();
        art.setAuthor(user.get());
        art.setPublicationTime(LocalDateTime.now());

        if (artDto.getImageFile() != null && !artDto.getImageFile().isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + artDto.getImageFile().getOriginalFilename();
                String s3Url = uploadFileToS3(artDto.getImageFile(), fileName);
                art.setImageUrl(s3Url);
            } catch (IOException | S3Exception e) {
                throw new RuntimeException("Ошибка при загрузке изображения в S3", e);
            }
        }
        art = artRepo.create(art);
        Set<String> tags = artDto.getTags().stream().map(TagDto::getName).collect(Collectors.toSet());
        List<String> list = tagRepo.getNotExistsTags(tags);
        for (String tag : list){
            ArtTag artTag = new ArtTag(art, tagRepo.create(new Tag(tag)));
            artTagRepo.create(artTag);
        }
        return modelMapper.map(art, ArtCreateDto.class);
    }

    @Transactional
    public ArtDto save(ArtUpdateDto arts) {
        Art art = modelMapper.map(arts, Art.class);
        if (arts.getImageFile() != null && !arts.getImageFile().isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + arts.getImageFile().getOriginalFilename();
                String s3Url = uploadFileToS3(arts.getImageFile(), fileName);
                art.setImageUrl(s3Url);
            } catch (IOException | S3Exception e) {
                throw new RuntimeException("Ошибка при обновлении изображения в S3", e);
            }
        }
        artRepo.save(art);
        tagService.updateListTagsInArt(arts.getTags(), art);
        return modelMapper.map(art, ArtDto.class);
    }

    private String uploadFileToS3(MultipartFile file, String fileName) throws IOException, S3Exception {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(fileName)).toExternalForm();
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
        return modelMapper.map(art, ArtDto.class);
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
        Optional<User> user = getCurrentUser();
        if (user.isEmpty())
            return;
        UUID uuid = UUID.fromString(artId);
        Art art = artRepo.findById(uuid).get();
        Interaction interaction = interactionRepo.findByArtIdAndUserId(uuid, user.get().getId(), false);
        if (interaction == null){
            interactionRepo.create(new Interaction(user.get(),art));
            art.setCountViews(art.getCountViews() + 1);
            artRepo.save(art);
        }
    }

    @Transactional
    public boolean likeArt(String artId){
        UUID uuid = UUID.fromString(artId);
        Art art = artRepo.findById(uuid).get();
        Optional<User> user = getCurrentUser();
        if (user.isEmpty())
            throw new AuthorizationException();
        Interaction interaction = interactionRepo.findByArtIdAndUserId(uuid, user.get().getId(), false);
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
        Optional<UUID> userId = getCurrentUserId();
        if (userId.isEmpty())
            throw new AuthorizationException();
        List<Interaction> likes = interactionRepo.findLikedByUser(userId.get(), true, false);

        List<ArtCardDto> likedArts = likes.stream()
                .map(Interaction::getArt)
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        List<ArtCardDto> paged = likedArts.subList((page - 1) * size, Math.min((page - 1) * size + size, likedArts.size()));

        return new PageImpl<>(paged, PageRequest.of(page - 1, size), likedArts.size());
    }


    public boolean isLikeArt(String artId){
        UUID uuid = UUID.fromString(artId);
        Optional<User> user = getCurrentUser();
        if (user.isEmpty())
            return false;
        Interaction interaction = interactionRepo.findByArtIdAndUserId(uuid, user.get().getId(), false);
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
        Optional<UUID> userId = getCurrentUserId();
        if (userId.isEmpty())
            throw new AuthorizationException();
        List<Interaction> interactions = interactionRepo.findWithArtTagsByUserId(userId.get(), false);

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
        Optional<UUID> userId = getCurrentUserId();
        if (userId.isEmpty())
            throw new AuthorizationException();
        List<Sub> subs = subRepo.findSubAndUserBySubscriberId(userId.get(), false);

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
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(60);
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

    private Jwt getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }


    private Optional<UUID> getCurrentUserId(){
        Jwt jwt = getJwt();
        if (jwt == null)
            return Optional.empty();
        return Optional.of(UUID.fromString(jwt.getSubject()));
    }

    private Optional<User> getCurrentUser(){
        Jwt jwt = getJwt();
        if (jwt == null)
            return Optional.empty();
        UUID id = UUID.fromString(jwt.getSubject());
        return userRepo.findById(id);
    }
}
