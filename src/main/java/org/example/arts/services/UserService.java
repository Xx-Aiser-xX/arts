package org.example.arts.services;

import org.example.arts.dtos.*;
import org.example.arts.dtos.create.RegisterUserDto;
import org.example.arts.dtos.update.UserUpdateDto;
import org.example.arts.entities.*;
import org.example.arts.exceptions.AuthorizationException;
import org.example.arts.exceptions.IncorrectDataException;
import org.example.arts.repo.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final SocialNetworkRepository socialNetworkRepo;
    private final UserPreferencesRepository userPreferencesRepo;
    private final SubRepository subRepo;
    private final ArtRepository artRepo;
    private final ModelMapper modelMapper;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${s3.avatars.prefix}")
    private String avatarsPrefix;

    @Autowired
    public UserService(UserRepository userRepo, SocialNetworkRepository socialNetworkRepo, UserPreferencesRepository userPreferencesRepo, SubRepository subRepo, ArtRepository artRepo, ModelMapper modelMapper, S3Client s3Client) {
        this.userRepo = userRepo;
        this.socialNetworkRepo = socialNetworkRepo;
        this.userPreferencesRepo = userPreferencesRepo;
        this.subRepo = subRepo;
        this.artRepo = artRepo;
        this.modelMapper = modelMapper;
        this.s3Client = s3Client;
    }

    @Transactional
    public UserDto registerUser(RegisterUserDto dto) {

        if (userRepo.findByUserName(dto.getUserName(), false).isPresent()) {
            throw new IncorrectDataException("Пользователь  с ником: " + dto.getUserName() + " уже существует");
        }
        User user = modelMapper.map(dto, User.class);

        userRepo.create(user);
        userPreferencesRepo.create(new UserPreferences(user));
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional
    public UserDto updateUser(UserUpdateDto dto) {
        UUID id = getCurrentUserId()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));

        User user = userRepo.findById(id)
                .orElseThrow(() -> new AuthorizationException("Пользователь не найден"));

        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUserName())) {
            if (userRepo.findByUserName(dto.getUserName(), false).isPresent()) {
                throw new IncorrectDataException("Пользователь  с ником: " + dto.getUserName() + " уже существует");
            }
            user.setUserName(dto.getUserName());
        }

        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            try {
                String fileName = avatarsPrefix + id.toString() + "_" + UUID.randomUUID().toString() + "_" + dto.getAvatarFile().getOriginalFilename();
                String s3Url = uploadFileToS3(dto.getAvatarFile(), fileName);
                user.setPhotoUrl(s3Url);
            } catch (IOException | S3Exception e) {
                throw new RuntimeException("Ошибка при обновлении аватара в S3", e);
            }
        }
        user.setDescription(dto.getDescription());

        if (!dto.getSocialNetwork().isEmpty()){
            for (SocialNetworkDto sn : dto.getSocialNetwork()){
                SocialNetwork socialNetwork = modelMapper.map(sn, SocialNetwork.class);
                socialNetwork.setUser(user);
                socialNetworkRepo.create(socialNetwork);
            }
        }

        return modelMapper.map(userRepo.save(user), UserDto.class);
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
    public void deleted(RegisterUserDto userDto){
        User user = modelMapper.map(userDto, User.class);
        user.setDeleted(true);
        userRepo.save(user);
    }

    public UserDto getCurrentUserDto() {
        User user = getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        return modelMapper.map(user, UserDto.class);
    }

    public UserMinDto getCurrentUserMinDto() {
        User user = getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        return modelMapper.map(user, UserMinDto.class);
    }

    public List<SocialNetworkDto> getSocialNetworkUser(String id) {
        UUID uuid = UUID.fromString(id);
        List<SocialNetwork> socialNetworks = socialNetworkRepo.findByUserAndDeletedFalse(uuid, false);
        return socialNetworks.stream().map(sn -> modelMapper.map(sn, SocialNetworkDto.class)).toList();
    }

    public Page<SubDto> getSubscriptions(Integer page, Integer size){
        Page<Sub> subs = subRepo.getPageEntities(page, size, false);
        Page<Sub> sort = new PageImpl<>(
                subs.stream()
                        .sorted(Comparator.comparing(Sub::getSubscriptionDate).reversed())
                        .toList(),
                subs.getPageable(),
                subs.getTotalElements()
        );
        return sort.map(sub -> modelMapper.map(sub, SubDto.class));
    }

    @Transactional
    public SubscribeDto subscribe(String idAuthor){
        UUID uuid = UUID.fromString(idAuthor);
        User user = getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        User author = userRepo.findById(uuid)
                .orElseThrow(() -> new AuthorizationException("Автор не найден"));;
        Sub sub = subRepo.signed(user.getId(), author.getId());
        if (!user.equals(author)) {
            if (sub == null) {
                subRepo.create(new Sub(user, author));
            } else if (sub.isDeleted()) {
                sub.setDeleted(false);
                subRepo.save(sub);
            } else {
                sub.setDeleted(true);
                subRepo.save(sub);
            }
            return modelMapper.map(sub, SubscribeDto.class);
        }
        return null;
    }

    public boolean isSubscribe(String authorId){
        UUID uuid = UUID.fromString(authorId);
        User user = getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        User author = userRepo.findById(uuid)
                .orElseThrow(() -> new AuthorizationException("Автор не найден"));
        Sub sub = subRepo.signed(user.getId(), author.getId());
        return (sub != null && !sub.isDeleted());
    }

    private User getUserById(String id){
        UUID uuid = UUID.fromString(id);
        return userRepo.findById(uuid).get();
    }

    public UserDto getUser(String id){
        User user = getUserById(id);
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional(readOnly = true)
    public Page<SubWithArtsDto> getSubscriptionsWithArts(int artsPerAuthor, Integer page, Integer size) {
        UUID userUUID = getCurrentUserId()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        List<Sub> subs = subRepo.findBySubscriberId(userUUID, false);
        subs.sort(Comparator.comparing(Sub::getSubscriptionDate).reversed());

        List<Sub> paginatedSubs = subs.subList((page - 1) * size,
                Math.min((page - 1) * size + size, subs.size()));

        List<SubWithArtsDto> result = paginatedSubs.stream()
                .map(sub -> {
                    User author = sub.getTarget();
                    List<Art> allArts = artRepo.findByAuthor(author.getId(), false);
                    List<ArtCardDto> artCards = allArts.stream()
                            .sorted(Comparator.comparing(Art::getPublicationTime).reversed())
                            .limit(artsPerAuthor)
                            .map(art -> modelMapper.map(art, ArtCardDto.class))
                            .toList();

                    SubWithArtsDto dto = new SubWithArtsDto();
                    dto.setAuthor(modelMapper.map(author, UserMinDto.class));
                    dto.setArts(artCards);
                    return dto;
                }).toList();
        return new PageImpl<>(result, PageRequest.of(page - 1, size), subs.size());
    }

    public List<UserMinDto> getRecentSubscriptions(int limit) {
        UUID userId = getCurrentUserId()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        List<Sub> allSubs = subRepo.findBySubscriber(userId, false);

        return allSubs.stream()
                .sorted(Comparator.comparing(Sub::getSubscriptionDate).reversed())
                .limit(limit)
                .map(sub -> modelMapper.map(sub.getTarget(), UserMinDto.class))
                .toList();
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