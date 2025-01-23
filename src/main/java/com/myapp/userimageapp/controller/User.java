package com.myapp.userimageapp.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.myapp.userimageapp.model.UserModel;
import com.myapp.userimageapp.service.AuthService;
import com.myapp.userimageapp.service.ImageService;
import com.myapp.userimageapp.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class User {
 
    //autowiring dependencies
    @Autowired  
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private AuthService authService;

    //we set these values in applications.properties as env variables 
    @Value("${imgur.clientId}")
    private String imgurClientId;

    @Value("${imgur.clientSecret}")
    private String imgurClientSecret;

    @Value("${imgur.redirectUri}")
    private String imgurRedirectUri;

    // Register User API call
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestParam String firstname, @RequestParam String lastname, @RequestParam String username, @RequestParam String password) {
        if (userService.authenticateUser(username, password) != null) {
            log.warn("Username {} already exists. User needs to authorize.", username);
            return ResponseEntity.badRequest().body("Username already exists!Proceed to authorize.");
        }
        userService.registerUser(firstname, lastname, username, password);
        log.info("User {} successfully registered.", username);
        return ResponseEntity.ok("User successfully registered, proceed to authorize");
    }

    // Redirect user to Imgur's OAuth authorization page
    @GetMapping("/authorize")
    public ResponseEntity<String> authorizeUser(@RequestParam String username) {
        log.info("In authorize process..");
        if (!userService.isUserAlreadyRegistered(username)) {
            log.warn("User not registered!");
            return ResponseEntity.badRequest().body("Please register first.");
        }
        String authorizationUrl = "https://api.imgur.com/oauth2/authorize" +
                "?client_id=" + imgurClientId +
                "&response_type=code" +
                "&redirect_uri=" +  URLEncoder.encode(imgurRedirectUri, StandardCharsets.UTF_8);
        
        log.info("User Authorized successfully!");

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authorizationUrl)
                .build();
    }

    //After authorize call, this callback method is called to get access token for requests to Imgur
    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String authorizationCode) {
        log.info("In callback process..");
        try {
            // Exchange the authorization code for an access token
            String accessToken = authService.exchangeAuthCodeForAccessToken(authorizationCode);
            // Store the access token in your system 
            log.info("Callback success. Access token provided.");
            return ResponseEntity.ok("Access Token: " + accessToken);
        } catch (Exception e) {
            log.error("Error exchanging authorization code");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error exchanging authorization code: " + e.getMessage());
        }
    }

    // Upload image to Imgur (use OAuth access token)
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) {
        log.info("In Upload process..");
        String accessToken = extractAccessTokenFromAuthHeader(authHeader);
        if (accessToken == null) {
            log.warn("User not authenticated!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }

        try {
            Map<String, Object> image = imageService.uploadImage(file, accessToken);
            log.info("Image upload successful!");
            return ResponseEntity.ok((String) image.get("link"));
        } catch (IOException ex) {
            log.error("Upload failed!");
            return ResponseEntity.status(500).body("Failed to upload image: " + ex.getMessage());
        }
    }

    // View image (use OAuth access token)
    @GetMapping("/{imageId}")
    public ResponseEntity<Map<String, Object>> getImage(@PathVariable String imageId, @RequestHeader("Authorization") String authHeader) {
        log.info("In user image retrieval..");
        // Extract access token from the header
        String accessToken = extractAccessTokenFromAuthHeader(authHeader);
        if (accessToken == null) {
            log.warn("User not authenticated");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Map<String,Object> image = imageService.getImage(imageId, accessToken);
        if (image == null) {
            log.warn("Image not found!");
            return ResponseEntity.status(404).body(null);
        }
        log.info("Image retrieved successfully!");
        return ResponseEntity.ok(image);
    }

    // Delete image from Imgur (use OAuth access token)
    @DeleteMapping("/{deleteHash}")
    public ResponseEntity<String> deleteImage(@PathVariable String deleteHash, @RequestHeader("Authorization") String authHeader) {
        log.info("In Image deletion...");
        // Extract access token from the header
        String accessToken = extractAccessTokenFromAuthHeader(authHeader);
        if (accessToken == null) {
            log.warn("User not authenticated!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }

        Boolean isDeleted = imageService.deleteImage(deleteHash, accessToken);
        if (isDeleted) {
            log.info("Image deleted successfully!");
            return ResponseEntity.ok("Image deleted successfully");
        } else {
            log.warn("Image not found");
            return ResponseEntity.status(404).body("Image not found");
        }
    }

    // View all images of a specific user from Imgur (use OAuth access token)
    @GetMapping("/user/images")
    public ResponseEntity<List<Map<String, Object>>> getAllImages(@RequestHeader("Authorization") String authHeader) {
        log.info("In all images retrieval process..");
        // Extract access token from the header
        String accessToken = extractAccessTokenFromAuthHeader(authHeader);
        if (accessToken == null) {
            log.warn("User not authenticated!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonList(Collections.singletonMap("error", "User not authenticated")));
        }

        try {
            List<Map<String, Object>> userImages = imageService.getUserImages(accessToken);
            log.info("User images retrieved successfully!");
            return ResponseEntity.ok(userImages);
        } catch (Exception e) {
            log.error("Images retrieval failed!");
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    //API call to get details of the current authenticated user
    @GetMapping("/user-details")
    public ResponseEntity<UserModel> getUserDetails(@RequestParam String username, @RequestHeader("Authorization") String authHeader){
        
        log.info("In user details process..");
        String accessToken = extractAccessTokenFromAuthHeader(authHeader);

        if (accessToken == null) {
            log.warn("User not authenticated");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        log.info("User details retrieval successful!");
        UserModel user=userService.getUserInfo(username);
        return ResponseEntity.ok(user);
    }

    // Extract the access token from the Authorization header
    private String extractAccessTokenFromAuthHeader(String authHeader) {
        log.info("Extracting access token from authentication header..");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // Extract the token (remove "Bearer " prefix)
        }
        log.warn("Invalid token!");
        return null;  // Return null if the token is invalid or not present
    }
}
