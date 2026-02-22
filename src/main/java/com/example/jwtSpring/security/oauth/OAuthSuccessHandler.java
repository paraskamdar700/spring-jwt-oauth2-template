package com.example.jwtSpring.security.oauth;

import com.example.jwtSpring.user.User;
import com.example.jwtSpring.user.UserRepo;
import com.example.jwtSpring.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepo userRepo;
    private final JwtUtils jwtUtils;

    @Autowired
    public OAuthSuccessHandler(UserRepo userRepo, JwtUtils jwtUtils) {
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken =
                (OAuth2AuthenticationToken) authentication;

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String provider = authToken.getAuthorizedClientRegistrationId(); // Ensure the provider is available in OAuth2User attributes
        String providerId = oauthUser.getAttribute("sub"); // Commonly used for user IDs (e.g. for Google)

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found", "Email not provided by OAuth provider", null)
            );
        }

        Optional<User> userOptional = userRepo.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = new User();
            user.setEmail(email);
            user.setFullname(name);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setRoles("USER");
            userRepo.save(user);
        }


//        String token = jwtUtils.generateToken(user.getEmail());
//        response.setContentType("application/json");
//        response.getWriter().write("""
//                {
//                    "message": "OAuth login successful",
//                    "token": "%s"
//                }
//                """.formatted(token));

//
        String token = jwtUtils.generateToken(user.getEmail());

        String redirectUrl = "http://127.0.0.1:8081/auth/?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}
