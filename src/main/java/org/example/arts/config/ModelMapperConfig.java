package org.example.arts.config;

import org.example.arts.dtos.CommentDto;
import org.example.arts.entities.Comment;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {


    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Comment.class, CommentDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getAuthor().getId(), CommentDto::setAuthorId);
            mapper.map(src -> src.getAuthor().getUserName(), CommentDto::setAuthorUserName);
        });

        return modelMapper;
    }


}