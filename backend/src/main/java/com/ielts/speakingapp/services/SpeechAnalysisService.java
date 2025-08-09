package com.ielts.speakingapp.services;

import com.ielts.speakingapp.models.PracticeSession;
import com.ielts.speakingapp.models.User;
import com.ielts.speakingapp.models.dto.SpeechAnalysisResponse;
import com.ielts.speakingapp.repositories.PracticeSessionRepository;
import com.ielts.speakingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpeechAnalysisService {

    private final SpeechToTextService speechToTextService;
    private final DeepSeekAnalysisService deepSeekAnalysisService;
    private final TextToSpeechService textToSpeechService;
    private final PracticeSessionRepository practiceSessionRepository;
    private final UserRepository userRepository;

    public SpeechAnalysisResponse analyzeSpeech(String question, String base64Audio, String audioFormat, String username, String testSection) {
        try {
            log.info("Starting speech analysis for question: {}", question);
            
            // Get user
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Step 1: Transcribe audio to text
            String transcript = speechToTextService.transcribeAudio(base64Audio, audioFormat);
            log.info("Transcription completed: {}", transcript);
            
            // Step 2: Analyze with DeepSeek AI
            SpeechAnalysisResponse analysis = deepSeekAnalysisService.analyzeSpeech(question, transcript);
            log.info("AI analysis completed with overall score: {}", analysis.getOverallScore());
            
            // Step 3: Generate audio feedback
            String feedbackAudio = textToSpeechService.synthesizeSpeech(analysis.getFeedback());
            analysis.setFeedbackAudioUrl("data:audio/mp3;base64," + feedbackAudio);
            log.info("Audio feedback generated");
            
            // Step 4: Save to database
            PracticeSession session = new PracticeSession();
            session.setUser(user);
            session.setQuestion(question);
            session.setTestSection(testSection);
            session.setTranscript(transcript);
            session.setFeedback(analysis.getFeedback());
            session.setOverallScore(analysis.getOverallScore());
            session.setFluencyScore(analysis.getFluencyScore());
            session.setLexicalScore(analysis.getLexicalScore());
            session.setGrammaticalScore(analysis.getGrammaticalScore());
            session.setPronunciationScore(analysis.getPronunciationScore());
            session.setAudioUrl("data:audio/" + audioFormat + ";base64," + base64Audio);
            session.setFeedbackAudioUrl(analysis.getFeedbackAudioUrl());
            session.setCreatedAt(LocalDateTime.now());
            
            practiceSessionRepository.save(session);
            log.info("Practice session saved with ID: {}", session.getId());
            
            return analysis;
            
        } catch (Exception e) {
            log.error("Error in speech analysis workflow", e);
            throw new RuntimeException("Failed to analyze speech", e);
        }
    }
}
