package org.example.arts.services;

import org.example.arts.dtos.KeycloakUserDto;
import org.example.arts.dtos.create.KeycloakRegisterDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KeycloakService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    public KeycloakUserDto registerUser(KeycloakRegisterDto request) {
        String adminToken = getAdminToken();

        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", request.getUsername());
        payload.put("email", request.getEmail());
        payload.put("enabled", true);

        System.out.println("!!!!!!!!" + request.getUsername() + "!!!!!!!!" + request.getEmail() + "!!!!!!!!" + request.getPassword());
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", request.getPassword());
        credential.put("temporary", false);

        payload.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String locationHeader = response.getHeaders().getFirst("Location");
            if (locationHeader != null) {
                // http://localhost:8080/admin/realms/{realm}/users/{id}
                String[] segments = locationHeader.split("/");
                String userId = segments[segments.length - 1];
                return new KeycloakUserDto(userId, request.getUsername(), request.getEmail());
            }
            else {
                throw new RuntimeException("Пользователь создан, но ID не получен (нет Location заголовка)");
            }
        }
        else {
            throw new RuntimeException("Ошибка при регистрации пользователя в Keycloak: " + response.getStatusCode());
        }
    }


    private String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "admin-cli");
        params.add("username", adminUsername);
        params.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }

        throw new RuntimeException("Не удалось получить токен администратора Keycloak");
    }
}
