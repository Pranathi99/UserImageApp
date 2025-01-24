package com.myapp.userimageapp.controllerTest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.myapp.userimageapp.controller.User;
import com.myapp.userimageapp.model.UserModel;
import com.myapp.userimageapp.service.ImageService;
import com.myapp.userimageapp.service.UserService;

@SpringBootTest
@TestPropertySource(properties = {"imgur.clientId=sampleClientId", "imgur.redirectUri=sampleRedirectUri"})
public class UserTest {

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
        String authHeader = "Bearer accessToken";
        String accessToken = "accessToken"; // Extracted from the auth header
        String imageUrl = "https://picsum.photos/id/237";
        MultipartFile file = mock(MultipartFile.class);  // Mock MultipartFile
        String expectedResponse = imageUrl;

        Map<String,Object>responseEntity=Map.of("link", imageUrl);

        when(imageService.uploadImage(eq(file), eq(accessToken)))
                .thenReturn(responseEntity);

        String actualResponse=userController.uploadImage(file,authHeader).getBody();
        assertEquals(actualResponse, expectedResponse);
    }

    // Test Get Image API
    @Test
    public void testGetImage() throws Exception {
        String imageId = "image123";
        String accessToken = "accessToken";
        String authHeader = "Bearer accessToken";

        Map<String, Object> expectedResponse = Map.of("id", imageId, "url", "https://picsum.photos/id/237");

        when(imageService.getImage(imageId, accessToken)).thenReturn(expectedResponse);

        Map<String,Object> actualResponse=userController.getImage(imageId,authHeader).getBody();
        assertEquals(actualResponse, expectedResponse);
    }

    // Test Delete Image API
    @Test
    public void testDeleteImage() throws Exception {
        String deleteHash = "delete123";
        String accessToken = "accessToken";
        String authHeader = "Bearer accessToken";
        String expectedResponse="Image deleted successfully";

        when(imageService.deleteImage(deleteHash, accessToken)).thenReturn(true);

        String actualResponse=userController.deleteImage(deleteHash,authHeader).getBody();
        assertEquals(actualResponse, expectedResponse);
    }

    @Test
    public void testGetAllImages() throws Exception {
        String authHeader = "Bearer accessToken";
        String accessToken="accessToken";
        Map<String, Object> image1 = new HashMap<>();
        image1.put("id", "image123");
        image1.put("url", "https://example.com/image1.jpg");

        Map<String, Object> image2 = new HashMap<>();
        image2.put("id", "image234");
        image2.put("url", "https://example.com/image2.jpg");

        // Mock the response body to return a list of maps under the "data" key
        List<Map<String, Object>> mockImageList = new ArrayList<>();
        mockImageList.add(image1);
        mockImageList.add(image2);

        when(imageService.getUserImages(accessToken)).thenReturn(mockImageList);

        List<Map<String, Object>> actualResponse=userController.getAllImages(authHeader).getBody();

        assertEquals(mockImageList, actualResponse);
        
    }

    // Test Get User Details API
    @Test
    public void testGetUserDetails() throws Exception {
        String username = "john_summers";
        String authHeader = "Bearer accessToken";
        UserModel user = new UserModel("John", "Summers",username);

        when(userService.getUserInfo(username)).thenReturn(user);
        UserModel actualResponse=userController.getUserDetails(username,authHeader).getBody();

        assertEquals(actualResponse, user);
    }


}
