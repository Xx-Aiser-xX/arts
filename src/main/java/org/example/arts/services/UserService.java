package org.example.arts.services;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.arts.dtos.SocialNetworkDto;
import org.example.arts.dtos.SubscribeDto;
import org.example.arts.dtos.UserDto;
import org.example.arts.dtos.UserMinDto;
import org.example.arts.dtos.create.RegisterUserDto;
import org.example.arts.dtos.update.UserUpdateDto;
import org.springframework.data.domain.Page;
import java.util.*;

public interface UserService {
    UserDto registerUser(RegisterUserDto dto);
    UserDto updateUser(UserUpdateDto dto) throws FileUploadException;
    void deleted(String id);
    UserDto getCurrentUserDto();
    UserMinDto getCurrentUserMinDto();
    List<SocialNetworkDto> getSocialNetworkUser(String id);
    SubscribeDto subscribe(String idAuthor);
    boolean isSubscribe(String authorId);
    UserDto getUser(String id);
    Page<UserMinDto> getSubscriptionsWithArts(Integer page, Integer size);
    List<UserMinDto> getRecentSubscriptions(int limit);
}
