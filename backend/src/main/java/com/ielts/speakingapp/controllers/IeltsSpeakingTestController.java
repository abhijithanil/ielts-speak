package com.ielts.speakingapp.controllers;

import com.ielts.speakingapp.models.dto.SpeechAnalysisResponse;
import com.ielts.speakingapp.security.JwtUtil;
import com.ielts.speakingapp.services.IeltsSpeakingTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/ielts-test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class IeltsSpeakingTestController {

    private final IeltsSpeakingTestService ieltsSpeakingTestService;
    private final JwtUtil jwtUtil;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateCompleteTest(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }

            log.info("Generating complete IELTS speaking test with structured format");
            Map<String, Object> testData = ieltsSpeakingTestService.generateCompleteTest();
            
            return ResponseEntity.ok(testData);
        } catch (Exception e) {
            log.error("Error generating complete test: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate-structured")
    public ResponseEntity<Map<String, Object>> generateStructuredTest(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("Received request to generate structured IELTS test");
            
            // Validate token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token provided for structured test generation");
                return ResponseEntity.status(401).build();
            }

            log.info("Generating structured IELTS speaking test with DeepSeek-R1-0528 model");
            Map<String, Object> testData = ieltsSpeakingTestService.generateCompleteTest();
            
            log.info("Successfully generated structured test with {} questions", 
                testData.get("totalQuestions") != null ? testData.get("totalQuestions") : "unknown");
            
            return ResponseEntity.ok(testData);
        } catch (Exception e) {
            log.error("Error generating structured test: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyze-question")
    public ResponseEntity<SpeechAnalysisResponse> analyzeQuestionResponse(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Validate token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }

            String question = request.get("question");
            String base64Audio = request.get("audioData");
            String audioFormat = request.get("audioFormat");
            String section = request.get("section");

            log.info("Analyzing question response for section: {}", section);
            
            SpeechAnalysisResponse analysis = ieltsSpeakingTestService.analyzeQuestionResponse(
                question, base64Audio, audioFormat, section);
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error analyzing question response: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/summary")
    public ResponseEntity<Map<String, Object>> getTestSummary(
            @RequestBody List<SpeechAnalysisResponse> allAnalyses,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Validate token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }

            log.info("Generating test summary for {} analyses", allAnalyses.size());
            
            Map<String, Object> summary = ieltsSpeakingTestService.getTestSummary(allAnalyses);
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error generating test summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTestStatus() {
        try {
            Map<String, Object> status = Map.of(
                "status", "ready",
                "message", "IELTS Speaking Test service is running",
                "features", List.of(
                    "Complete test generation",
                    "Sequential question processing",
                    "Real-time speech analysis",
                    "Comprehensive feedback"
                )
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting test status: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
