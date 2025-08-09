package com.ielts.speakingapp.controllers;

import com.ielts.speakingapp.models.dto.AuthResponse;
import com.ielts.speakingapp.models.dto.LoginRequest;
import com.ielts.speakingapp.models.dto.SignupRequest;
import com.ielts.speakingapp.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        log.info("Signup request received for username: {}", request.getUsername());

        try {
            AuthResponse response = authService.signup(request);

            if (response.getToken() != null) {
                log.info("Signup successful for user: {}", request.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Signup failed for user: {} - {}", request.getUsername(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error during signup for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest().body(
                    new AuthResponse(null, null, "Signup failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());

        try {
            AuthResponse response = authService.login(request);

            if (response.getToken() != null) {
                log.info("Login successful for user: {}", request.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Login failed for user: {} - {}", request.getUsername(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error during login for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest().body(
                    new AuthResponse(null, null, "Login failed: " + e.getMessage())
            );
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Token validation request received");

        try {
            // Token validation logic would go here
            return ResponseEntity.ok(new AuthResponse(null, null, "Token is valid"));
        } catch (Exception e) {
            log.error("Error during token validation", e);
            return ResponseEntity.badRequest().body(
                    new AuthResponse(null, null, "Token validation failed")
            );
        }
    }
}