package org.example.arts.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;

@Service
public class TaggerService {

    private final WebClient taggerWebClient;

    @Autowired
    public TaggerService(WebClient taggerWebClient) {
        this.taggerWebClient = taggerWebClient;
    }

    public List<String> getTagsForImage(MultipartFile file) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            String[] tags = taggerWebClient.post()
                    .uri("/tags")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(String[].class)
                    .block();
            return tags != null ? List.of(tags) : List.of();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + e.getMessage());
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Ошибка от сервиса тегирования: " + e.getResponseBodyAsString());
        }
    }
}
