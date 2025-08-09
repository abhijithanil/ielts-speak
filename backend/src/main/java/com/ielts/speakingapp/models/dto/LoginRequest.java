package com.ielts.speakingapp.models.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
