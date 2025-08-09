package com.ielts.speakingapp.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeechAnalysisRequest {
    private String question;
    private String audioData; // Base64 encoded audio
    private String audioFormat; // e.g., "wav", "mp3"
    private String testSection; // "part1", "part2", "part3"
}
