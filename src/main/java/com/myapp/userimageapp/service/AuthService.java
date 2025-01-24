package com.myapp.userimageapp.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

     @Value("${imgur.clientId}")
    private String clientId;

    @Value("${imgur.clientSecret}")
    private String clientSecret;

    @Value("${imgur.redirectUri}")
    private String redirectUri;

    private static final String IMGUR_TOKEN_URL = "https://api.imgur.com/oauth2/token";

    // In-memory storage for user access tokens (use persistent storage in production)
    private final Map<String, String> userAccessTokens = new HashMap<>();

    //private final RestTemplate restTemplate = new RestTemplate();

    private final RestTemplate restTemplate;

    // Constructor injection
    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Default constructor for scenarios where you don't want dependency injection
    public AuthService() {
        this.restTemplate = new RestTemplate(); // default RestTemplate if no injection
    }

    // Exchange authorization code for an access token
    public String exchangeAuthCodeForAccessToken(String authorizationCode) {
        log.info("In exchanging authentication code for access token process..");
        try{
            // Prepare request data
            MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
            requestData.add("client_id", clientId);
            requestData.add("client_secret", clientSecret);
            requestData.add("code", authorizationCode);
            requestData.add("grant_type", "authorization_code");
            requestData.add("redirect_uri", redirectUri);

            // Set headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Create HttpEntity with data and headers
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestData, headers);

            // Make POST request to Imgur's token endpoint
            ResponseEntity<Map> response = restTemplate.exchange(IMGUR_TOKEN_URL, HttpMethod.POST, entity, Map.class);

            // Parse and return the access token
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Access token retrieved!");
                return (String) responseBody.get("access_token");
            } else {
                log.warn("Failed to retrieve access token!");
                throw new RuntimeException("Failed to retrieve access token");
            }
        }
        catch(Exception ex)
        {
            log.error(ex.getMessage());
            return ex.getMessage();
        }
    }

    // Store access token for a user
    public void storeAccessToken(String username, String accessToken) {
        log.info("Storing access token in local..");
        userAccessTokens.put(username, accessToken);
    }

    // Get the stored access token for a user
    public String getAccessTokenForUser(String username) {
        log.info("Access token for a user..");
        return userAccessTokens.get(username);
    }
}
