package org.example.arts.services;

import org.example.arts.dtos.KeycloakUserDto;
import org.example.arts.dtos.create.KeycloakRegisterDto;
import org.example.arts.exceptions.KeycloakAuthenticationException;
import org.example.arts.exceptions.KeycloakIntegrationException;
import org.example.arts.exceptions.KeycloakUserExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
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
        String adminToken = null;
        try {
            adminToken = getAdminToken();
        } catch (KeycloakAuthenticationException e) {
            throw new KeycloakIntegrationException("Ошибка внутренней аутентификации Keycloak: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new KeycloakIntegrationException("Непредвиденная ошибка при подготовке к регистрации пользователя: " + e.getMessage(), e);
        }
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", request.getUsername());
        payload.put("email", request.getEmail());
        payload.put("enabled", true);

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", request.getPassword());
        credential.put("temporary", false);

        payload.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String locationHeader = response.getHeaders().getFirst("Location");
                if (locationHeader != null) {
                    // http://localhost:8080/admin/realms/{realm}/users/{id}
                    String[] segments = locationHeader.split("/");
                    String userId = segments[segments.length - 1];
                    return new KeycloakUserDto(userId, request.getUsername(), request.getEmail());
                } else {
                    throw new KeycloakIntegrationException("Пользователь зарегистрирован, но ID не получен от Keycloak.");
                }
            } else {
                throw new KeycloakIntegrationException("Неожиданный ответ от Keycloak при регистрации пользователя: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException.Conflict e) {
            throw new KeycloakUserExistsException("Пользователь с таким ником уже существует.", e);
        } catch (HttpClientErrorException e) {
            throw new KeycloakIntegrationException("Ошибка при регистрации пользователя в Keycloak: " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            throw new KeycloakIntegrationException("Внутренняя ошибка Keycloak при регистрации пользователя: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new KeycloakIntegrationException("Невозможно подключиться к Keycloak или другая ошибка сети: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new KeycloakIntegrationException("Непредвиденная ошибка при регистрации пользователя: " + e.getMessage(), e);
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
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                if (accessToken != null) {
                    return accessToken;
                } else {
                    throw new KeycloakAuthenticationException("Токен администратора Keycloak не найден в ответе.");
                }
            } else {
                throw new KeycloakAuthenticationException("Неожиданный ответ от Keycloak при получении токена администратора: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new KeycloakAuthenticationException("Неверные учетные данные администратора Keycloak.", e);
        } catch (HttpClientErrorException e) {
            throw new KeycloakIntegrationException("Ошибка при получении токена администратора Keycloak: " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            throw new KeycloakIntegrationException("Внутренняя ошибка Keycloak при получении токена администратора: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new KeycloakIntegrationException("Невозможно подключиться к Keycloak или другая ошибка сети: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new KeycloakIntegrationException("Непредвиденная ошибка при получении токена администратора Keycloak: " + e.getMessage(), e);
        }
    }
}
