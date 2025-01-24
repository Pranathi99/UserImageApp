package com.myapp.userimageapp.serviceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.myapp.userimageapp.model.UserModel;
import com.myapp.userimageapp.repository.UserRepository;
import com.myapp.userimageapp.service.ImageService;
import com.myapp.userimageapp.service.UserService;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private ImageService imageService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        // Initialize the password encoder
        passwordEncoder = new BCryptPasswordEncoder();
    }   
    
    @Test
    public void testRegisterUser() {
        String firstname = "John";
        String lastname = "Summers";
        String username = "john_summers";
        String password = "john@1234";

        // Prepare a mock user
        UserModel user = new UserModel();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // Mock the repository save method
        when(userRepo.save(any(UserModel.class))).thenReturn(user);

        // Call the registerUser method
        UserModel registeredUser = userService.registerUser(firstname, lastname, username, password);

        // Assert the result
        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertTrue(passwordEncoder.matches(password, registeredUser.getPassword()));

        // Verify that save was called once
        verify(userRepo, times(1)).save(any(UserModel.class));
    }

    @Test
    public void testIsUserAlreadyRegistered() {
        String username = "john_summers";

        // Mock the repository method existsByUsername
        when(userRepo.existsByUsername(username)).thenReturn(true);

        // Call the isUserAlreadyRegistered method
        boolean isRegistered = userService.isUserAlreadyRegistered(username);

        // Assert the result
        assertTrue(isRegistered);

        // Verify that existsByUsername was called once
        verify(userRepo, times(1)).existsByUsername(username);
    }

    @Test
    public void testAuthenticateUser_Success() {
        String username = "john_summers";
        String password = "john@1234";

        // Create a mock user
        UserModel user = new UserModel();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // Mock the repository method findByUsername
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));

        // Call authenticateUser
        UserModel authenticatedUser = userService.authenticateUser(username, password);

        // Assert the result
        assertNotNull(authenticatedUser);
        assertEquals(username, authenticatedUser.getUsername());

        // Verify that findByUsername was called once
        verify(userRepo, times(1)).findByUsername(username);
    }

    @Test
    public void testAuthenticateUser_Failure() {
        String username = "john_summers";
        String password = "johnny@1234";

        // Mock the repository method findByUsername to return an empty Optional (user not found)
        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Call authenticateUser
        UserModel authenticatedUser = userService.authenticateUser(username, password);

        // Assert the result
        assertNull(authenticatedUser);

        // Verify that findByUsername was called once
        verify(userRepo, times(1)).findByUsername(username);
    }

    @Test
    public void testGetUserInfo() {
        String username = "john_summers";

        // Create a mock user
        UserModel user = new UserModel();
        user.setUsername(username);

        // Mock the repository method findByUsername
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));

        // Call getUserInfo
        UserModel foundUser = userService.getUserInfo(username);

        // Assert the result
        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());

        // Verify that findByUsername was called once
        verify(userRepo, times(1)).findByUsername(username);
    }
}
