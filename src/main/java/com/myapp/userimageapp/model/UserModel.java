package com.myapp.userimageapp.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;

    @Column(unique = true,nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private List<String> imageUrls;  // List to hold Imgur image URLs

    // Add the custom constructor with three parameters
    public UserModel(String firstname, String lastname, String username) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
    }
}