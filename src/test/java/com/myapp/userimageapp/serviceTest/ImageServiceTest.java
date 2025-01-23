package com.myapp.userimageapp.serviceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import com.myapp.userimageapp.service.AuthService;
import com.myapp.userimageapp.service.ImageService;

@SpringBootTest
public class ImageServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ImageService imageService;

    @Mock 
    private AuthService authService;

    @InjectMocks
    private ImageServiceTest imageServiceTest;

    @Test
    void testExchangeAuthCodeForAccessToken_Success() {
        
        String authorizationCode = "auth_code_123";  // The authorization code we're passing in the test
        String expectedAccessToken = "access_token_123";  // The access token we expect to receive in the mock response

        // Mock the response to simulate the Imgur API's response to the authorization code exchange
        Map<String, Object> mockResponse = Map.of("access_token", expectedAccessToken);
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Mock restTemplate.exchange() to return the mock response when called
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // Act: Call the method under test
        String accessToken = authService.exchangeAuthCodeForAccessToken(authorizationCode);

        // Assert: Verify that the access token is extracted correctly
        assertEquals(expectedAccessToken, accessToken);  // The method should return the mock token
    }

    @Test
    public void testUploadImage() throws Exception {
        String access_token = "access_token";
    
        // Prepare a mock image file as a byte array
        byte[] imageContent = "image content".getBytes();  // This is just an example. Replace with actual image content in real cases

        // Create a MockMultipartFile with the filename, content type, and file content
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",                       // The form field name for the file upload
                "test-image.jpg",             // The filename of the uploaded file
                "image/jpeg",                 // The content type of the file
                imageContent                  // The byte array representing the file content
        );

        Map<String, Object> expectedResponse = new HashMap<>();
        
        // This needs to be a mock method, so make sure you're calling it on the mocked imageService
        when(imageService.uploadImage(imageFile, access_token)).thenReturn(expectedResponse);

        Map<String, Object> actualResponse = imageService.uploadImage(imageFile, access_token);  // This should be the same mock call
        
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        
        verify(imageService, times(1)).uploadImage(imageFile, access_token);  // Verify the method was called
    }

    @Test
    public void testGetImage() throws Exception {
        String imageId = "image123";
        String accessToken = "access_token_123";
        Map<String, Object> mockResponse = Map.of("id", imageId, "url", "https://example.com/image.jpg");

        // Mock the response from restTemplate
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // Act: Call the method under test
        Map<String, Object> actualResponse = imageService.getImage(imageId, accessToken);

        // Assert: Verify that the image data is returned correctly
        assertNotNull(actualResponse);  // Ensure that the response is not null
        assertEquals(imageId, actualResponse.get("id"));
        assertEquals("https://example.com/image.jpg", actualResponse.get("url"));

        // Verify the external call to restTemplate was made
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    }

    @Test
    public void testDeleteImageSuccess() throws Exception {
        String imageId = "mock-image-id";
        String accessToken = "access_token";
        Boolean mockResponse = true; // Assuming this is what the service returns when deletion is successful

        // Mock the external call (e.g., restTemplate.exchange) that `deleteImage` uses internally
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act: Call the method under test
        Boolean actualResponse = imageService.deleteImage(imageId, accessToken);

        // Assert: Verify the result
        assertNotNull(actualResponse);
        assertEquals(mockResponse, actualResponse);  // The returned value should be true as expected

        // Verify that the method interacts with restTemplate correctly
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Boolean.class));
    }

    @Test
    public void testDeleteImageFail() throws Exception {
        String imageId = "mock-image-id";
        String accessToken = "access_token";
        Boolean mockResponse = false; // Assuming this is what the service returns when deletion is successful

        // Mock the external call (e.g., restTemplate.exchange) that `deleteImage` uses internally
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act: Call the method under test
        Boolean actualResponse = imageService.deleteImage(imageId, accessToken);

        // Assert: Verify the result
        assertNotNull(actualResponse);
        assertEquals(mockResponse, actualResponse);  // The returned value should be true as expected

        // Verify that the method interacts with restTemplate correctly
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Boolean.class));
    }

}
