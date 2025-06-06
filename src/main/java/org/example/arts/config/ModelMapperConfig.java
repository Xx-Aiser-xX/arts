package org.example.arts.config;

import org.example.arts.dtos.ArtDto;
import org.example.arts.dtos.UserDto;
import org.example.arts.entities.Art;
import org.example.arts.entities.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.typeMap(Art.class, ArtDto.class).addMappings(mapper -> {
//            mapper.map(Art::getAuthor, ArtDto::setAuthor);
//        });
        return modelMapper;
    }
}