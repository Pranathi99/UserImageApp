package com.myapp.userimageapp.serviceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.myapp.userimageapp.service.AuthService;
import com.myapp.userimageapp.service.ImageService;

@SpringBootTest
public class ImageServiceTest {

    @Test
    void testExchangeAuthCodeForAccessToken_Success() {
        
        String authorizationCode = "auth_code_123";  // The authorization code we're passing in the test
        String expectedAccessToken = "access_token_123";  // The access token we expect to receive in the mock response

         // Mock the response body to simulate the Imgur API's response to the authorization code exchange
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("access_token", expectedAccessToken);

        // Mock the ResponseEntity to return the mock response with status OK
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Create a mock RestTemplate and inject it into the AuthService
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // Create the AuthService instance with the mocked RestTemplate
        AuthService authService = new AuthService(mockRestTemplate);

        // Act: Call the method under test
        String accessToken = authService.exchangeAuthCodeForAccessToken(authorizationCode);

        // Assert: Verify that the access token is extracted correctly
        assertEquals(expectedAccessToken, accessToken);  // The method should return the mock token

        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));

    }

    @Test
    public void testUploadImageSuccess() throws Exception {
        String access_token="access_token_123";
        MultipartFile file=mock(MockMultipartFile.class); 
        String imageUrl = "https://imgur.com/someimage.jpg";

        //Prepare the mock response from Imgur API
        Map<String,Object>mockResponse=new HashMap<>();
        Map<String,Object>data=new HashMap<>();
        data.put("link",imageUrl);
        mockResponse.put("data", data);

        // Mock RestTemplate's exchange method to return the mock response
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);
        
        ImageService imageService=new ImageService(mockRestTemplate);

        Map<String,Object>actualResponse=imageService.uploadImage(file, access_token);
        //System.out.println(actualResponse.get("link"));
        assertNotNull(actualResponse);
        assertTrue(actualResponse.containsKey("link"));
        assertEquals(imageUrl, actualResponse.get("link"));

        // Verify that RestTemplate's exchange method was called
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testUploadImage_Failure_Exception() throws IOException {
        String access_token="access_token_123";
        MultipartFile file=mock(MockMultipartFile.class); 

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Failed"));

        ImageService imageService=new ImageService(mockRestTemplate);

        Map<String,Object>actualResponse=imageService.uploadImage(file, access_token);

        assertNull(actualResponse);

        verify(mockRestTemplate,times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test 
    public void testUploadImage_Failure_NoData() throws IOException{
        String access_token="access_token_123";
        MultipartFile file=mock(MockMultipartFile.class); 

        Map<String,Object>mockResponse=new HashMap<>();
        mockResponse.put("error", "error");

        ResponseEntity<Map>mockResponseEntity=new ResponseEntity<Map>(mockResponse, HttpStatus.OK);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        ImageService imageService=new ImageService(mockRestTemplate);

        Map<String,Object>actualResponse=imageService.uploadImage(file, access_token);

        assertNull(actualResponse);

        verify(mockRestTemplate,times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    public void testGetImage_Success() throws Exception {
        String imageId = "image123";
        String accessToken = "access_token_123";
        String url="https://example.com/image.jpg";
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String,Object>data=Map.of("id", imageId, "url",url );
        mockResponse.put("data", data);

        // Mock the response from restTemplate
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // Act: Call the method under test
        ImageService imageService=new ImageService(mockRestTemplate);

        Map<String, Object> actualResponse = imageService.getImage(imageId, accessToken);

        // Assert: Verify that the image data is returned correctly
        assertNotNull(actualResponse);  // Ensure that the response is not null
        assertEquals(imageId, actualResponse.get("id"));
        assertEquals(url, actualResponse.get("url"));

        // Verify the external call to restTemplate was made
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    public void testGetImage_Failure() throws Exception {
        String imageId = "image123";
        String accessToken = "access_token_123";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("error", "error");

        // Mock the response from restTemplate
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // Act: Call the method under test
        ImageService imageService=new ImageService(mockRestTemplate);

        Map<String, Object> actualResponse = imageService.getImage(imageId, accessToken);

        // Assert: Verify that the image is not retrieved 
        assertNull(actualResponse);

        // Verify the external call to restTemplate was made
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }


    @Test
    public void testDeleteImage_Success() throws Exception {
        String imageId = "mock-image-id";
        String url="https://example.com/image.jpg";
        String accessToken = "access_token";

        Map<String, Object> data = Map.of("id", imageId, "url", url);
        Map<String, Object> mockResponseBody = Map.of("data", data);
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);
    
        RestTemplate mockRestTemplate=mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act: Call the method under test
        ImageService imageService=new ImageService(mockRestTemplate);
        Boolean actualResponse = imageService.deleteImage(imageId, accessToken);

        // Assert: Verify the result
        assertNotNull(actualResponse);
        assertEquals(true, actualResponse);  // The returned value should be true as expected

        // Verify that the method interacts with restTemplate correctly
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    public void testDeleteImageFail() throws Exception {
        String imageId = "mock-image-id";
        String accessToken = "access_token";

        Map<String, Object> mockResponseBody = Map.of("error", "error");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.NOT_FOUND);
    
        RestTemplate mockRestTemplate=mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act: Call the method under test
        ImageService imageService=new ImageService(mockRestTemplate);
        Boolean actualResponse = imageService.deleteImage(imageId, accessToken);

        // Assert: Verify the result
        assertNotNull(actualResponse);
        assertEquals(false, actualResponse);  // The returned value should be true as expected

        // Verify that the method interacts with restTemplate correctly
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class));
     }

    @Test
    public void testGetAllImage_Success() throws Exception {
        String accessToken = "access_token_123";

        // Mock the list of image data
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

        Map<String,List<Map<String, Object>>> mockResponse = new HashMap<>();
        mockResponse.put("data", mockImageList);

        // Mock the response from restTemplate
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // Act: Call the method under test
        ImageService imageService=new ImageService(mockRestTemplate);

        List<Map<String, Object>> actualResponse = imageService.getUserImages(accessToken);

        // Assert: Verify that the image data is returned correctly
        assertNotNull(actualResponse);  // Ensure that the response is not null
        assertEquals(mockResponse.get("data").size(), actualResponse.size());
        // Verify the external call to restTemplate was made
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    public void testGetAllImage_Failure_NoData() throws Exception {
        String accessToken = "access_token_123";

        Map<String,String> mockResponse = new HashMap<>();
        mockResponse.put("error", "error");

        // Mock the response from restTemplate
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // Act: Call the method under test
        ImageService imageService=new ImageService(mockRestTemplate);

        List<Map<String, Object>> actualResponse = imageService.getUserImages(accessToken);

        // Assert: Verify that the image data is returned correctly
        assertNull(actualResponse);  // Ensure that the response is not null
        // Verify the external call to restTemplate was made
        verify(mockRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }
}
