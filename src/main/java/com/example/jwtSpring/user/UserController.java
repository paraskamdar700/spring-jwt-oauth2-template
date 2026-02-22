package com.example.jwtSpring.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/profile")
    public String getProfile() {
        return "This is protected profile data";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Protected dashboard";
    }
}

