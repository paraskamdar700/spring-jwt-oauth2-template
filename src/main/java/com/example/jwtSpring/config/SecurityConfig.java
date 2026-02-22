package com.example.jwtSpring.config;

import com.example.jwtSpring.security.jwt.JwtAuthFilter;
import com.example.jwtSpring.security.oauth.CustomOidcUserService;
import com.example.jwtSpring.user.MyUserDetailService;
import com.example.jwtSpring.security.oauth.OAuthSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private MyUserDetailService userDetailService;
    private JwtAuthFilter jwtAuthFilter;
    private OAuthSuccessHandler oAuthSuccessHandler;
    private CustomOidcUserService customOidcUserService;

    @Autowired
    public SecurityConfig(MyUserDetailService userDetailService, JwtAuthFilter jwtAuthFilter, OAuthSuccessHandler oAuthSuccessHandler, CustomOidcUserService customOidcUserService) {
        this.userDetailService = userDetailService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuthSuccessHandler=oAuthSuccessHandler;
        this.customOidcUserService = customOidcUserService;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){

        http
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .exceptionHandling(ex ->
                    ex.authenticationEntryPoint((req, res,e) ->{
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized\"}");
                            })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2Login ->
                    oauth2Login.userInfoEndpoint(userInfoEndpoint ->
                                    userInfoEndpoint.oidcUserService(customOidcUserService)).successHandler(oAuthSuccessHandler)
                            .failureHandler((request, response, exception) -> {

                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                response.setContentType("application/json");

                                response.getWriter().write("""
                {
                    "error": "OAuth2 Authentication Failed",
                    "message": "%s"
                }
                """.formatted(exception.getMessage())
                                );
                            })
                );

                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }




}
