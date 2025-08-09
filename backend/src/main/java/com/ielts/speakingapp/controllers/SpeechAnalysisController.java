package com.ielts.speakingapp.controllers;

import com.ielts.speakingapp.models.dto.CurrentUser;
import com.ielts.speakingapp.models.dto.SpeechAnalysisRequest;
import com.ielts.speakingapp.models.dto.SpeechAnalysisResponse;
import com.ielts.speakingapp.models.dto.UserPrincipal;
import com.ielts.speakingapp.security.JwtTokenUtil;
import com.ielts.speakingapp.services.SpeechAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/v1/speech")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class SpeechAnalysisController {

    private final SpeechAnalysisService speechAnalysisService;
    private final JwtTokenUtil jwtUtil;

    @PostMapping("/analyze")
    public ResponseEntity<SpeechAnalysisResponse> analyzeSpeech(
            @RequestParam("question") String question,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "testSection", defaultValue = "part1") String testSection,
            @RequestHeader("Authorization") String authHeader, @CurrentUser UserPrincipal userPrincipal) {
        
        try {
            log.info("Received speech analysis request for question: {}", question);
            
            // Extract username from JWT token
            String username = extractUsernameFromToken(authHeader, userPrincipal);
            
            // Convert audio file to base64
            byte[] audioBytes = audioFile.getBytes();
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            
            // Determine audio format
            String audioFormat = getAudioFormat(audioFile.getOriginalFilename());
            
            // Analyze speech
            SpeechAnalysisResponse response = speechAnalysisService.analyzeSpeech(
                question, base64Audio, audioFormat, username, testSection);
            
            log.info("Speech analysis completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error processing audio file", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error analyzing speech", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyze-base64")
    public ResponseEntity<SpeechAnalysisResponse> analyzeSpeechBase64(
            @RequestBody SpeechAnalysisRequest request,
            @RequestHeader("Authorization") String authHeader, @CurrentUser UserPrincipal userPrincipal) {
        
        try {
            log.info("Received speech analysis request for question: {}", request.getQuestion());
            
            // Extract username from JWT token
            String username = extractUsernameFromToken(authHeader, userPrincipal);
            
            // Analyze speech
            SpeechAnalysisResponse response = speechAnalysisService.analyzeSpeech(
                request.getQuestion(), 
                request.getAudioData(), 
                request.getAudioFormat(),
                username,
                request.getTestSection() != null ? request.getTestSection() : "part1"
            );
            
            log.info("Speech analysis completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing speech", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String extractUsernameFromToken(String authHeader, UserPrincipal userPrincipal) {
        try {
            // Remove "Bearer " prefix
            String token = authHeader.replace("Bearer ", "");
            
            // Validate token and extract username
            if (jwtUtil.validateToken(token, userPrincipal)) {
                return jwtUtil.getUsernameFromToken(token);
            } else {
                throw new RuntimeException("Invalid JWT token");
            }
        } catch (Exception e) {
            log.error("Error extracting username from token", e);
            throw new RuntimeException("Invalid authentication token");
        }
    }

    private String getAudioFormat(String filename) {
        if (filename == null) return "wav";
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "mp3" -> "mp3";
            case "wav" -> "wav";
            case "m4a" -> "m4a";
            case "ogg" -> "ogg";
            default -> "wav";
        };
    }
}
