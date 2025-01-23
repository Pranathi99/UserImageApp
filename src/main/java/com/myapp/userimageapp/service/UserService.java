package com.myapp.userimageapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.myapp.userimageapp.model.UserModel;
import com.myapp.userimageapp.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //User registration
    public UserModel registerUser(String firstname,String lastname,String username,String password)
    {
        log.info("Registering user in user service..");
        UserModel user=new UserModel();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        log.info("User registered successfully!");
        return userRepo.save(user);
    }

    public boolean isUserAlreadyRegistered(String username) {
        log.info("Checking if user is already registered..");
        return userRepo.existsByUsername(username);
    }

    //Authenticating user
    public UserModel authenticateUser(String username,String password)
    {
        log.info("In process of checking if user is authenticated in user service..");
        UserModel user=userRepo.findByUsername(username).orElse(null);
        if(user!=null && passwordEncoder.matches(password, user.getPassword()))
        {
            log.info("User is authenticated as credentials match!");
            return user;
        }
        log.warn("User credentials does not match!");
        return null;
    }

    //Retrieving user details
    public UserModel getUserInfo(String username)
    {
        return userRepo.findByUsername(username).orElse(null);
    }
}
