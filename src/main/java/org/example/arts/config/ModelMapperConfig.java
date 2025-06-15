package org.example.arts.config;

import org.example.arts.dtos.CommentDto;
import org.example.arts.dtos.update.ArtUpdateDto;
import org.example.arts.entities.Art;
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

        modelMapper.createTypeMap(ArtUpdateDto.class, Art.class).addMappings(mapper -> {
            mapper.skip(Art::setImageUrl);
            mapper.skip(Art::setAuthor);
        });

        return modelMapper;
    }


}