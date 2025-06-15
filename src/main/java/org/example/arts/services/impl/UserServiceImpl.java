package org.example.arts.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.arts.dtos.*;
import org.example.arts.dtos.create.RegisterUserDto;
import org.example.arts.dtos.update.UserUpdateDto;
import org.example.arts.entities.*;
import org.example.arts.exceptions.AuthorizationException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.arts.exceptions.IncorrectDataException;
import org.example.arts.repo.*;
import org.example.arts.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@EnableCaching
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final SocialNetworkRepository socialNetworkRepo;
    private final UserPreferencesRepository userPreferencesRepo;
    private final SubRepository subRepo;
    private final ArtRepository artRepo;
    private final ModelMapper modelMapper;
    private final S3Client s3Client;
    private final CurrentUserService currentUserService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${s3.avatars.prefix}")
    private String avatarsPrefix;

    @Autowired
    public UserServiceImpl(UserRepository userRepo, SocialNetworkRepository socialNetworkRepo, UserPreferencesRepository userPreferencesRepo, SubRepository subRepo, ArtRepository artRepo, ModelMapper modelMapper, S3Client s3Client, CurrentUserService currentUserService) {
        this.userRepo = userRepo;
        this.socialNetworkRepo = socialNetworkRepo;
        this.userPreferencesRepo = userPreferencesRepo;
        this.subRepo = subRepo;
        this.artRepo = artRepo;
        this.modelMapper = modelMapper;
        this.s3Client = s3Client;
        this.currentUserService = currentUserService;
    }

    @Transactional
    @CacheEvict(value = "user", key = "#dto.id.toString()")
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
    @CacheEvict(value = {"user", "social-network"}, key = "#dto.id")
    public UserDto updateUser(UserUpdateDto dto) throws FileUploadException {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизован"));

        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUserName())) {
            if (userRepo.findByUserName(dto.getUserName(), false).isPresent()) {
                throw new IncorrectDataException("Пользователь  с ником: " + dto.getUserName() + " уже существует");
            }
            user.setUserName(dto.getUserName());
        }

        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            try {
                String fileName = avatarsPrefix + user.getId().toString() + "_" + UUID.randomUUID().toString() + "_" + dto.getAvatarFile().getOriginalFilename();
                String s3Url = uploadFileToS3(dto.getAvatarFile(), fileName);
                user.setPhotoUrl(s3Url);
            } catch (IOException | S3Exception e) {
                throw new FileUploadException("Ошибка при обновлении аватара в S3", e);
            }
        }
        user.setDescription(dto.getDescription());
        updateUserSocialNetworks(user, dto.getSocialNetwork());

        return modelMapper.map(userRepo.save(user), UserDto.class);
    }

    private void updateUserSocialNetworks(User user, List<SocialNetworkDto> newSocialNetworkDtos) {
        List<SocialNetwork> currentSocialNetworks = socialNetworkRepo.findByUserId(user.getId(), false);

        Map<String, SocialNetwork> currentSocialNetworksMap = currentSocialNetworks.stream()
                .collect(Collectors.toMap(SocialNetwork::getLink, Function.identity()));

        for (SocialNetworkDto newSnDto : newSocialNetworkDtos) {
            String newSnLink = newSnDto.getLink();
            SocialNetwork existingSn = currentSocialNetworksMap.get(newSnLink);

            if (existingSn != null) {
                if (existingSn.isDeleted()) {
                    existingSn.setDeleted(false);
                    socialNetworkRepo.save(existingSn);
                }
                currentSocialNetworksMap.remove(newSnLink);
            } else {
                SocialNetwork newSocialNetwork = modelMapper.map(newSnDto, SocialNetwork.class);
                newSocialNetwork.setUser(user);
                newSocialNetwork.setDeleted(false);
                socialNetworkRepo.create(newSocialNetwork);
            }
        }

        for (SocialNetwork snToDeactivate : currentSocialNetworksMap.values()) {
            if (!snToDeactivate.isDeleted()) {
                snToDeactivate.setDeleted(true);
                socialNetworkRepo.save(snToDeactivate);
            }
        }
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
    @CacheEvict(value = "user", key = "#id")
    public void deleted(String id){
        User user = getUserById(id);
        User currentUser = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизован"));
        if (!user.equals(currentUser))
            throw new AuthorizationException("Вы не являетесь владельцем данного аккаунта");
        user.setDeleted(true);
        userRepo.save(user);
    }

    public UserDto getCurrentUserDto() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        return modelMapper.map(user, UserDto.class);
    }

    public UserMinDto getCurrentUserMinDto() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        return modelMapper.map(user, UserMinDto.class);
    }

    @Cacheable(value = "social-network", key = "#id")
    public List<SocialNetworkDto> getSocialNetworkUser(String id) {
        UUID uuid = UUID.fromString(id);
        List<SocialNetwork> socialNetworks = socialNetworkRepo.findByUserId(uuid, false);
        return socialNetworks.stream().map(sn -> modelMapper.map(sn, SocialNetworkDto.class)).toList();
    }

    @Transactional
    public SubscribeDto subscribe(String idAuthor){
        UUID uuid = UUID.fromString(idAuthor);
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        User author = userRepo.findById(uuid)
                .orElseThrow(() -> new AuthorizationException("Автор не найден"));;
        Optional<Sub> sub = subRepo.signed(user.getId(), author.getId());
        if (!user.equals(author)) {
            if (sub.isEmpty()) {
                Sub sub1 = subRepo.create(new Sub(user, author));
                return modelMapper.map(sub1, SubscribeDto.class);
            } else if (sub.get().isDeleted()) {
                sub.get().setDeleted(false);
                subRepo.save(sub.get());
            } else {
                sub.get().setDeleted(true);
                subRepo.save(sub.get());
            }
            return modelMapper.map(sub.get(), SubscribeDto.class);
        }
        else
            throw new IllegalArgumentException("Пользователь не может подписаться сам на себя");
    }


    public boolean isSubscribe(String authorId){
        UUID uuid = UUID.fromString(authorId);
        Optional<User> user = currentUserService.getCurrentUser();
        if (user.isEmpty())
            return false;
        User author = userRepo.findById(uuid)
                .orElseThrow(() -> new AuthorizationException("Автор не найден"));
        Optional<Sub> sub = subRepo.signed(user.get().getId(), author.getId());
        return (sub.isPresent() && !sub.get().isDeleted());
    }


    private User getUserById(String id){
        UUID uuid = UUID.fromString(id);
        return userRepo.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Cacheable(value = "user", key = "#id")
    public UserDto getUser(String id){
        User user = getUserById(id);
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional(readOnly = true)
    public Page<UserMinDto> getSubscriptionsWithArts(Integer page, Integer size) {
        UUID userUUID = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        List<Sub> subs = subRepo.findBySubscriberId(userUUID, false);
        subs.sort(Comparator.comparing(Sub::getSubscriptionDate).reversed());

        List<Sub> paginatedSubs = subs.subList((page - 1) * size,
                Math.min((page - 1) * size + size, subs.size()));
        List<UserMinDto> result = paginatedSubs.stream().map(sub -> modelMapper.map(sub.getTarget(), UserMinDto.class)).toList();
        return new PageImpl<>(result, PageRequest.of(page - 1, size), subs.size());
    }

    public List<UserMinDto> getRecentSubscriptions(int limit) {
        UUID userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new AuthorizationException("Пользователь не авторизирован"));
        List<Sub> allSubs = subRepo.findBySubscriber(userId, false);

        return allSubs.stream()
                .sorted(Comparator.comparing(Sub::getSubscriptionDate).reversed())
                .limit(limit)
                .map(sub -> modelMapper.map(sub.getTarget(), UserMinDto.class))
                .toList();
    }
}