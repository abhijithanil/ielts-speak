package com.ielts.speakingapp.models.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String username;
    private String message;
    
    public AuthResponse(String token, String username, String message) {
        this.token = token;
        this.username = username;
        this.message = message;
    }
}
