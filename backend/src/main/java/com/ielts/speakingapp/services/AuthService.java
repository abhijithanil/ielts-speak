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
        try {
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

            User savedUser = userRepository.save(user);

            // Create UserPrincipal and generate token
            UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
            String token = jwtTokenUtil.generateToken(userPrincipal);

            return new AuthResponse(token, savedUser.getUsername(), "User registered successfully");

        } catch (Exception e) {
            return new AuthResponse(null, null, "Signup failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
            if (userOpt.isEmpty()) {
                return new AuthResponse(null, null, "Invalid username or password");
            }

            User user = userOpt.get();

            // Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return new AuthResponse(null, null, "Invalid username or password");
            }

            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Create UserPrincipal and generate token
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String token = jwtTokenUtil.generateToken(userPrincipal);

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return new AuthResponse(token, user.getUsername(), "Login successful");

        } catch (Exception e) {
            return new AuthResponse(null, null, "Login failed: " + e.getMessage());
        }
    }
}