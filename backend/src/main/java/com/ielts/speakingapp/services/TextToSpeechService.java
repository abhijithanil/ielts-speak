package com.ielts.speakingapp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class TextToSpeechService {

    @Value("${google.cloud.text-to-speech.voice-name:en-US-Standard-A}")
    private String voiceName;

    @Value("${google.cloud.text-to-speech.language-code:en-US}")
    private String languageCode;

    public String synthesizeSpeech(String text) {
        // Mock implementation for development
        log.info("Mock text-to-speech synthesis for text length: {}", text.length());
        
        // Return a mock base64 audio (empty audio data)
        return Base64.getEncoder().encodeToString("mock-audio-data".getBytes());
    }
}
