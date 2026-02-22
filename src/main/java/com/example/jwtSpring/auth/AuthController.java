package com.example.jwtSpring.auth;

import com.example.jwtSpring.user.User;
import com.example.jwtSpring.user.MyUserDetailService;
import com.example.jwtSpring.user.UserServiceIMPL;
import com.example.jwtSpring.security.jwt.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private MyUserDetailService myUserDetailService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserServiceIMPL  userServiceIMPL;

    @GetMapping("/")
    public String index() {
        return "index welcome";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User userObj, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userObj.getEmail(), userObj.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String token = jwtUtils.generateToken(userObj.getEmail());
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day

            response.addCookie(cookie);

        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user)
    {
        return userServiceIMPL.addUser(user);
    }




}
