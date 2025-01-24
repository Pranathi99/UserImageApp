package com.myapp.userimageapp.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageService {

    @Value("${imgur.clientId}")
    private String clientId;

    @Value("${imgur.clientSecret}")
    private String clientSecret;

    @Value("${imgur.redirectUri}")
    private String redirectUri;

    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
    private static final String IMGUR_ACCOUNT_URL = "https://api.imgur.com/3/account/me/images";

    private final RestTemplate restTemplate;

    public ImageService(RestTemplate restTemplate)
    {
        this.restTemplate=restTemplate;
    }

    public ImageService()
    {
        this.restTemplate= new RestTemplate();
    }

    // Upload an image to Imgur using OAuth access token
    public Map<String, Object> uploadImage(MultipartFile imageFile, String accessToken) throws IOException {
        log.info("In image upload process in image service..");
        try{
            // Prepare the headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);  // Use OAuth Bearer token
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);


            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", imageFile.getResource());  // Add the image file to the request
            body.add("type", "file");
            body.add("public", "true"); // Ensure the image is public


            // Wrap everything in HttpEntity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send the request to Imgur's API
            ResponseEntity<Map> response = restTemplate.exchange(
                    IMGUR_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            // If successful, extract and return the image data
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    log.info("Image upload successful in image service...");
                    return (Map<String, Object>) responseBody.get("data");
                } else {
                    throw new IOException("Failed to upload image: No 'data' field in response");
                }
            } else {
                throw new IOException("Imgur API responded with status " + response.getStatusCode());
            }
        }
        catch(Exception ex)
        {
            log.error(ex.getMessage());
            return null;
        }
    }

    // Get image from Imgur by ID
    public Map<String, Object> getImage(String imageId, String accessToken) {
        log.info("In image retrieval process..");
        try{
            String url = IMGUR_API_URL + "/"+imageId;

            // Set the headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);  // Use OAuth Bearer token

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Make a GET request to Imgur API
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

            // Check if the response is successful and return image link
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Image retrieved successfully in image service..");
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return data;
            }
            else
            {
                log.warn("Image retrieval failed in image service..");
                return null;
            }
        }   
        catch(Exception ex)
        {
            log.error(ex.getMessage());
            return null;  // Return null if the request failed
        }
    }

    // Delete an image from Imgur
    public boolean deleteImage(String deleteHash, String accessToken) {
        log.info("In image deletion process in image service..");

        String url = IMGUR_API_URL + "/"+ deleteHash;

        // Set the headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);  // Use OAuth Bearer token

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Send DELETE request to Imgur API
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);
        //System.out.println(response);

        return response.getStatusCode() == HttpStatus.OK;
    }

    // Get all images of a user
    public List<Map<String, Object>> getUserImages(String accessToken) {
        log.info("In retrieval of all images of a user process...");
        String url = IMGUR_ACCOUNT_URL;
        // Set the headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Send GET request to Imgur API
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Retrieval success in image service..");
                System.out.println(responseBody.get("data"));
                return (List<Map<String, Object>>) responseBody.get("data");
            }
            else 
            {
                // Log status code and response body for better debugging
                log.error("Failed to retrieve images. Status code: " + response.getStatusCode());
                log.error("Response body: " + response.getBody());
                throw new RuntimeException("Error fetching images: " + response.getStatusCode());
            }
        }
        catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Http error occurred: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
            throw new RuntimeException("HTTP error fetching user images", ex);
        } catch (Exception ex) {
            log.error("An unexpected error occurred: " + ex.getMessage(), ex);
            throw new RuntimeException("Error fetching user images", ex);
        }
    }
}
