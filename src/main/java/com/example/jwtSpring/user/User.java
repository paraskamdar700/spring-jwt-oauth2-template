package com.example.jwtSpring.user;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name ="users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Fullname is required")
    private String fullname;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String password;

    @NotBlank(message = "role is required")
    private String roles;

    private String provider;   // "google" or "local"
    private String providerId; // Google's 'sub' ID
}
