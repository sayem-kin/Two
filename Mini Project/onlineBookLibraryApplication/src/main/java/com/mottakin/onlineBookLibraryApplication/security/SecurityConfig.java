package com.mottakin.onlineBookLibraryApplication.security;

import com.mottakin.onlineBookLibraryApplication.constants.AppConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {
        http
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->{
                    auth
                            .requestMatchers(HttpMethod.POST, AppConstants.SIGN_IN, AppConstants.SIGN_UP).permitAll()
                            .requestMatchers(HttpMethod.GET,"/users/hello").hasRole("USER")
                            .requestMatchers(HttpMethod.GET,"/users/hello2").hasRole("USER")
                            .requestMatchers(HttpMethod.GET,"/users/welcome").hasRole("admin")
                            .requestMatchers(HttpMethod.GET,"/users/{userId}").hasRole("admin")
                            .requestMatchers(HttpMethod.POST, "/users/create").permitAll()
                            .requestMatchers(HttpMethod.PUT, "/users/update").hasRole("admin")
                            .requestMatchers(HttpMethod.DELETE, "/users/delete/{id}").hasRole("admin")
                            .requestMatchers(HttpMethod.GET, "/users/all").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/borrow/{bookId}").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/return/{bookId}").permitAll()
                            .anyRequest().authenticated();
                })
                .addFilter(new CustomAuthenticationFilter(authenticationManager))
                .addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }
}
