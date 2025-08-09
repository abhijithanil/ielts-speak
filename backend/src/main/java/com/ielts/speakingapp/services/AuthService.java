package com.ielts.speakingapp.services;

import com.ielts.speakingapp.models.User;
import com.ielts.speakingapp.models.dto.AuthResponse;
import com.ielts.speakingapp.models.dto.LoginRequest;
import com.ielts.speakingapp.models.dto.SignupRequest;
import com.ielts.speakingapp.models.dto.UserPrincipal;
import com.ielts.speakingapp.repositories.UserRepository;
import com.ielts.speakingapp.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse signup(SignupRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(null, null, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, null, "Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Generate token
        String token = jwtTokenUtil.generateToken(userPrincipal);

        return new AuthResponse(token, user.getUsername(), "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return new AuthResponse(null, null, "Invalid username or password");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse(null, null, "Invalid username or password");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate token
        String token = jwtTokenUtil.generateToken(userPrincipal);

        return new AuthResponse(token, user.getUsername(), "Login successful");
    }
}
