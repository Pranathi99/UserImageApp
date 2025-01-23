package com.myapp.userimageapp.controllerTest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import com.myapp.userimageapp.controller.User;
import com.myapp.userimageapp.model.UserModel;
import com.myapp.userimageapp.service.ImageService;
import com.myapp.userimageapp.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"imgur.clientId=sampleClientId", "imgur.redirectUri=sampleRedirectUri"})
public class UserTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${imgur.clientId}")
    private String clientId;

    @Value("${imgur.redirectUri}")
    private String redirectUri;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private User userController;

    // Test Register User API
    @Test
    public void testRegisterUser() throws Exception {
        String expectedResponse="User successfully registered, proceed to authorize";
        when(userService.authenticateUser("john_summers", "john@1234")).thenReturn(null);

        String actualResponse=userController.registerUser("John", "Summers", "john_summers", "john@1234").getBody();

        assertEquals(expectedResponse, actualResponse);
    }

    // Test Image Upload API
    @Test
    public void testUploadImage() throws Exception {
        String accessToken = "accessToken";
        String imageUrl = "https://picsum.photos/id/237";
        MultipartFile file = mock(MultipartFile.class);  // You can mock the file as needed
        String expectedResponse="img_url";

        when(imageService.uploadImage(eq(file), eq(accessToken)))
                .thenReturn(Map.of("link", imageUrl));

        String actualResponse=userController.uploadImage(file,accessToken).getBody();
        assertEquals(actualResponse, expectedResponse);
    }

    // Test Get Image API
    @Test
    public void testGetImage() throws Exception {
        String imageId = "image123";
        String accessToken = "accessToken";
        Map<String, Object> expectedResponse = Map.of("id", imageId, "url", "https://picsum.photos/id/237");

        Map<String,Object> actualResponse=userController.getImage(imageId,accessToken).getBody();
        assertEquals(actualResponse, expectedResponse);
    }

    // Test Delete Image API
    @Test
    public void testDeleteImage() throws Exception {
        String deleteHash = "delete123";
        String accessToken = "accessToken";
        String expectedResponse="Image deleted successfully!";

        String actualResponse=userController.deleteImage(deleteHash,accessToken).getBody();
        assertEquals(actualResponse, expectedResponse);
    }

    // Test Get User Details API
    @Test
    public void testGetUserDetails() throws Exception {
        String username = "john_summers";
        String accessToken = "accessToken";
        UserModel user = new UserModel(username, "John", "Summers");

        UserModel actualResponse=userController.getUserDetails(username,accessToken).getBody();

        assertEquals(actualResponse, user);
    }
}
