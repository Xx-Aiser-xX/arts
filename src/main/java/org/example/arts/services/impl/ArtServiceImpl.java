package org.example.arts.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.arts.dtos.update.ArtUpdateDto;
import org.example.arts.entities.*;
import org.example.arts.exceptions.AuthorizationException;
import org.example.arts.exceptions.IncorrectDataException;
import org.example.arts.repo.*;
import org.example.arts.services.ArtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.example.arts.dtos.*;
import org.example.arts.dtos.create.ArtCreateDto;
import org.example.arts.exceptions.DataDeletedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
@EnableCaching
public class ArtServiceImpl implements ArtService {
    private final ArtRepository artRepo;
    private final TagRepository tagRepo;
    private final ArtTagRepository artTagRepo;
    private final InteractionRepository interactionRepo;
    private final SubRepository subRepo;
    private final TagServiceImpl tagService;
    private final CurrentUserService currentUserService;

    private final ModelMapper modelMapper;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${s3.arts.prefix}")
    private String artsPrefix;

    @Autowired
    public ArtServiceImpl(ArtRepository artRepo, TagRepository tagRepo, ArtTagRepository artTagRepo, InteractionRepository interactionRepo, SubRepository subRepo, TagServiceImpl tagService, CurrentUserService currentUserService, ModelMapper modelMapper, S3Client s3Client) {
        this.artRepo = artRepo;
        this.tagRepo = tagRepo;
        this.artTagRepo = artTagRepo;
        this.interactionRepo = interactionRepo;
        this.subRepo = subRepo;
        this.tagService = tagService;
        this.currentUserService = currentUserService;
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
    @Caching(evict = {
            @CacheEvict(value = "arts", allEntries = true),
            @CacheEvict(value = "arts-search", allEntries = true),
            @CacheEvict(value = "arts-author", allEntries = true)})
    public ArtCreateDto create(ArtCreateDto artDto) throws FileUploadException {
        Art art = modelMapper.map(artDto, Art.class);
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизован"));
        art.setAuthor(user);
        art.setPublicationTime(LocalDateTime.now());

        String s3Url = saveArtInS3(artDto.getImageFile());
        art.setImageUrl(s3Url);
        art = artRepo.create(art);
        Set<String> tags = artDto.getTags().stream().map(TagDto::getName).collect(Collectors.toSet());
        List<String> list = tagRepo.getNotExistsTags(tags);
        Set<Tag> set = tagRepo.getExistsTags(tags);
        for (String tag : list){
            Tag t = tagRepo.create(new Tag(tag));
            ArtTag artTag = new ArtTag(art, t);
            artTagRepo.create(artTag);
        }
        for (Tag tag : set){
            ArtTag artTag = new ArtTag(art, tag);
            artTagRepo.create(artTag);
        }
        return modelMapper.map(art, ArtCreateDto.class);
    }

    @Transactional
    @Caching(
            evict = { @CacheEvict(value = "arts-author", allEntries = true),
                    @CacheEvict(value = "arts-search", allEntries = true)},
            put = { @CachePut(value = "arts", key = "#artDto.id.toString()")})
    public ArtDto save(ArtUpdateDto artDto) throws FileUploadException {
        UUID artId = artDto.getId();
        Art art = artRepo.findById(artId)
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
//        modelMapper.map(artDto, art);
        art.setName(artDto.getName());
        art.setDescription(artDto.getDescription());
        art.setNsfw(artDto.isNsfw());
        if (artDto.getImageFile() != null && !artDto.getImageFile().isEmpty()) {
            String s3Url = saveArtInS3(artDto.getImageFile());
            art.setImageUrl(s3Url);
        }
        art = artRepo.save(art);
        if (artDto.getTags() != null) {
            tagService.updateListTagsInArt(artDto.getTags(), art);
        }
        return modelMapper.map(art, ArtDto.class);
    }

    private String saveArtInS3(MultipartFile imageFile) throws FileUploadException {
        if(imageFile == null || imageFile.isEmpty())
            throw new IncorrectDataException("Добавьте изображение");
        try {
            String fileName = artsPrefix + UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(imageFile.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize()));
            return s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(fileName)).toExternalForm();
        } catch (IOException | S3Exception e) {
            throw new FileUploadException("Ошибка при сохранении изображения в хранилище: " + e);
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "arts", key = "#artId"),
            @CacheEvict(value = "arts-author", allEntries = true)})
    public void deleted(String artId){
        UUID artUuid = UUID.fromString(artId);
        Art art = artRepo.findById(artUuid)
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизован"));
        if (!user.equals(art.getAuthor()))
            throw new AuthorizationException("Вы не являетесь владельцем Арта");
        art.setDeleted(true);
        artRepo.save(art);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "arts", key = "#id")
    public ArtDto findArtAndAuthorById(String id){
        UUID uuid = UUID.fromString(id);
        Art art = artRepo.findArtAndAuthorById(uuid, false)
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
        return modelMapper.map(art, ArtDto.class);
    }

    @Cacheable(value = "arts-author", key = "#id + ':' + #page + ':' + #size")
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
        Optional<User> user = currentUserService.getCurrentUser();
        if (user.isEmpty())
            return;
        UUID uuid = UUID.fromString(artId);
        Art art = artRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
        Optional<Interaction> interaction = interactionRepo.findByArtIdAndUserId(uuid, user.get().getId(), false);
        if (interaction.isEmpty()) {
            interactionRepo.create(new Interaction(user.get(),art));
            art.setCountViews(art.getCountViews() + 1);
            artRepo.save(art);
        }
    }

    @Transactional
    public boolean likeArt(String artId){
        UUID uuid = UUID.fromString(artId);
        Art art = artRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Арт не найден"));
        Optional<User> user = currentUserService.getCurrentUser();
        if (user.isEmpty())
            throw new AuthorizationException("Пользователь не авторизован");
        Optional<Interaction> interaction = interactionRepo.findByArtIdAndUserId(uuid, user.get().getId(), false);
        if (interaction.isPresent() && !interaction.get().isLike()) {
            interaction.get().setLike(true);
            interaction.get().setLikedAt(LocalDateTime.now());
            interactionRepo.save(interaction.get());
            art.setCountLikes(art.getCountLikes() + 1);
            artRepo.save(art);
            return true;
        }
        else if (interaction.isPresent() && interaction.get().getLikedAt() != null){
            interaction.get().setLike(false);
            interactionRepo.save(interaction.get());
            art.setCountLikes(art.getCountLikes() - 1);
            artRepo.save(art);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Page<ArtCardDto> getLikedArtsByCurrentUser(int page, int size) {
        Optional<UUID> userId = currentUserService.getCurrentUserId();
        if (userId.isEmpty())
            throw new AuthorizationException("Пользователь не авторизован");
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
        Optional<User> user = currentUserService.getCurrentUser();
        if (user.isEmpty())
            return false;
        Optional<Interaction> interaction = interactionRepo.findByArtIdAndUserId(uuid, user.get().getId(), false);
        return interaction.isPresent() && interaction.get().isLike();
    }

    public Page<ArtCardDto> getFeed(String type, int page, int size) {
        return switch (type) {
            case "trending" -> getTrendingArts(page, size);
            case "recommended" -> getRecommendedArts(page, size);
            case "subscriptions" -> getSubscribedArts(page, size);
            case "latest" -> getLatestArts(page, size);
            default -> throw new IncorrectDataException("Неправильный тип сортировки артов");
        };
    }

    @Cacheable(value = "arts-search", key = "#query + ':' + #page + ':' + #size")
    public Page<ArtCardDto> searchArtsByTagNameAndName(String query, int page, int size) {
        Set<Art> artsFromTags = artRepo.searchByTagName(query, false);
        Set<Art> artsFromName = artRepo.searchByName(query, false);

        List<ArtCardDto> tagArtsSorted = artsFromTags.stream()
                .sorted(Comparator.comparing(Art::getPublicationTime).reversed())
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        Set<Art> nameOnlyArts = new LinkedHashSet<>(artsFromName);
        nameOnlyArts.removeAll(artsFromTags);

        List<ArtCardDto> nameOnlyArtsSorted = nameOnlyArts.stream()
                .sorted(Comparator.comparing(Art::getPublicationTime).reversed())
                .map(art -> modelMapper.map(art, ArtCardDto.class))
                .toList();

        List<ArtCardDto> finalSortedList = new ArrayList<>();
        finalSortedList.addAll(tagArtsSorted);
        finalSortedList.addAll(nameOnlyArtsSorted);

        List<ArtCardDto> paged = finalSortedList.subList((page - 1) * size,
                Math.min((page - 1) * size + size, finalSortedList.size()));

        return new PageImpl<>(paged, PageRequest.of(page - 1, size), finalSortedList.size());
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
        Optional<UUID> userId = currentUserService.getCurrentUserId();
        if (userId.isEmpty())
            throw new AuthorizationException("Пользователь не авторизован");
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
        Optional<UUID> userId = currentUserService.getCurrentUserId();
        if (userId.isEmpty())
            throw new AuthorizationException("Пользователь не авторизован");
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
}
