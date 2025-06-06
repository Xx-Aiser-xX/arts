package org.example.arts.services;

import org.example.arts.dtos.*;
import org.example.arts.dtos.create.RegisterUserDto;
import org.example.arts.entities.*;
import org.example.arts.exceptions.IncorrectDataException;
import org.example.arts.repo.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final SocialNetworkRepository socialNetworkRepo;
    private final UserPreferencesRepository userPreferencesRepo;
    private final SubRepository subRepo;
    private final ArtRepository artRepo;
    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository userRepo, SocialNetworkRepository socialNetworkRepo, UserPreferencesRepository userPreferencesRepo, SubRepository subRepo, ArtRepository artRepo, ModelMapper modelMapper) {
        this.userRepo = userRepo;
        this.socialNetworkRepo = socialNetworkRepo;
        this.userPreferencesRepo = userPreferencesRepo;
        this.subRepo = subRepo;
        this.artRepo = artRepo;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public UserDto registerUser(RegisterUserDto dto) {
        UUID id = getCurrentUserId();
        if (userRepo.findById(id).isPresent()) {
            throw new IncorrectDataException("User with id " + id + " already exists");
        }
        if (userRepo.findByUserName(dto.getUserName(), false).isPresent()) {
            throw new IncorrectDataException("Username " + dto.getUserName() + " is already taken");
        }
        User user = new User(id, dto.getUserName(), dto.getPhotoUrl());
        UserDto userDto = modelMapper.map(user, UserDto.class);
        userPreferencesRepo.create(new UserPreferences(user));
        return userDto;
    }

    @Transactional
    public UserDto updateUser(UpdateUserDto dto) {
        UUID id = getCurrentUserId();

        User user = userRepo.findById(id)
                .orElseThrow(() -> new IncorrectDataException("User not found"));

        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUserName())) {
            if (userRepo.findByUserName(dto.getUserName(), false).isPresent()) {
                throw new IncorrectDataException("Username " + dto.getUserName() + " is already taken");
            }
            user.setUserName(dto.getUserName());
        }
        if (dto.getPhotoUrl() != null) {
            user.setPhotoUrl(dto.getPhotoUrl());
        }
        if (dto.getDescription() != null) {
            user.setDescription(dto.getDescription());
        }

        if (!dto.getSocialNetwork().isEmpty()){
            for (SocialNetworkDto sn : dto.getSocialNetwork()){
                SocialNetwork socialNetwork = modelMapper.map(sn, SocialNetwork.class);
                socialNetwork.setUser(user);
                socialNetworkRepo.create(socialNetwork);
            }
        }

        return modelMapper.map(userRepo.save(user), UserDto.class);
    }

    @Transactional
    public void deleted(RegisterUserDto userDto){
        User user = modelMapper.map(userDto, User.class);
        user.setDeleted(true);
        userRepo.save(user);
    }

    public UserDto getCurrentUserDto() {
        return modelMapper.map(getCurrentUser(), UserDto.class);
    }

    public UserMinDto getCurrentUserMinDto() {
        return modelMapper.map(getCurrentUser(), UserMinDto.class);
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
        User user = getCurrentUser();
        User author = userRepo.findById(uuid).get();
        Sub sub = subRepo.signed(uuid, author.getId());
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

    public boolean isSubscribe(String idAuthor){
        UUID uuid = UUID.fromString(idAuthor);
        User author = userRepo.findById(uuid).get();
        Sub sub = subRepo.signed(uuid, author.getId());
        return (sub == null || sub.isDeleted());
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
        UUID userUUID = getCurrentUserId();
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
        UUID userId = getCurrentUserId();
        List<Sub> allSubs = subRepo.findBySubscriber(userId, false);

        return allSubs.stream()
                .sorted(Comparator.comparing(Sub::getSubscriptionDate).reversed())
                .limit(limit)
                .map(sub -> modelMapper.map(sub.getTarget(), UserMinDto.class))
                .toList();
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