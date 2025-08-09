package com.ielts.speakingapp.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ielts.speakingapp.models.dto.IeltsTestResponse;
import com.ielts.speakingapp.models.dto.SpeechAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class IeltsSpeakingTestService {

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private SpeechToTextService speechToTextService;

    @Autowired
    private DeepSeekAnalysisService deepSeekAnalysisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generateCompleteTest() {
        try {
            log.info("Generating complete IELTS speaking test using new structured format with DeepSeek-R1-0528");
            
            // Generate full IELTS test using the new method
            String fullTestJson = deepSeekService.generateFullIeltsTest();
            log.info("Received full test JSON from DeepSeek, length: {}", fullTestJson.length());
            
            if (fullTestJson == null || fullTestJson.trim().isEmpty()) {
                log.error("Received empty or null JSON from DeepSeek");
                throw new RuntimeException("Empty response from DeepSeek API");
            }
            
            // Parse the JSON response
            IeltsTestResponse ieltsTest;
            try {
                ieltsTest = objectMapper.readValue(fullTestJson, IeltsTestResponse.class);
                log.info("Successfully parsed IELTS test response");
            } catch (Exception e) {
                log.error("Failed to parse JSON response: {}", e.getMessage());
                log.error("Raw JSON: {}", fullTestJson);
                throw new RuntimeException("Failed to parse DeepSeek response", e);
            }
            
            Map<String, Object> testData = new HashMap<>();
            List<Map<String, Object>> questions = new ArrayList<>();

            // Process Part 1 questions
            if (ieltsTest.getPart1() != null && ieltsTest.getPart1().getQuestions() != null) {
                log.info("Processing {} Part 1 questions", ieltsTest.getPart1().getQuestions().size());
                for (int i = 0; i < ieltsTest.getPart1().getQuestions().size(); i++) {
                    Map<String, Object> part1Question = new HashMap<>();
                    part1Question.put("section", "part1");
                    part1Question.put("question", ieltsTest.getPart1().getQuestions().get(i));
                    part1Question.put("description", "Introduction & Interview - Questions about familiar topics");
                    part1Question.put("duration", "4-5 minutes");
                    part1Question.put("maxRecordingTime", 60); // 60 seconds per question
                    part1Question.put("order", i + 1);
                    questions.add(part1Question);
                }
            } else {
                log.warn("Part 1 questions not found in response");
            }

            // Process Part 2 question
            if (ieltsTest.getPart2() != null) {
                log.info("Processing Part 2 question: {}", ieltsTest.getPart2().getTopic());
                Map<String, Object> part2 = new HashMap<>();
                part2.put("section", "part2");
                part2.put("question", ieltsTest.getPart2().getTopic());
                part2.put("bulletPoints", ieltsTest.getPart2().getBulletPoints());
                part2.put("description", "Individual Long Turn - Speak for 1-2 minutes on a specific topic");
                part2.put("duration", "3-4 minutes");
                part2.put("maxRecordingTime", 120); // 2 minutes for Part 2
                part2.put("order", questions.size() + 1);
                questions.add(part2);
            } else {
                log.warn("Part 2 question not found in response");
            }

            // Process Part 3 questions
            if (ieltsTest.getPart3() != null && ieltsTest.getPart3().getDiscussionQuestions() != null) {
                log.info("Processing {} Part 3 questions", ieltsTest.getPart3().getDiscussionQuestions().size());
                for (int i = 0; i < ieltsTest.getPart3().getDiscussionQuestions().size(); i++) {
                    Map<String, Object> part3Question = new HashMap<>();
                    part3Question.put("section", "part3");
                    part3Question.put("question", ieltsTest.getPart3().getDiscussionQuestions().get(i));
                    part3Question.put("description", "Two-Way Discussion - Deeper discussion on abstract topics");
                    part3Question.put("duration", "4-5 minutes");
                    part3Question.put("maxRecordingTime", 60); // 60 seconds per question
                    part3Question.put("order", questions.size() + 1);
                    questions.add(part3Question);
                }
            } else {
                log.warn("Part 3 questions not found in response");
            }

            if (questions.isEmpty()) {
                log.error("No questions were generated from the response");
                throw new RuntimeException("No questions generated from DeepSeek response");
            }

            testData.put("questions", questions);
            testData.put("totalQuestions", questions.size());
            testData.put("estimatedDuration", "11-14 minutes");
            testData.put("testId", UUID.randomUUID().toString());
            testData.put("createdAt", new Date());
            testData.put("originalResponse", ieltsTest); // Store the original parsed response

            log.info("Generated complete test with {} questions", questions.size());
            return testData;

        } catch (Exception e) {
            log.error("Error generating complete test: {}", e.getMessage(), e);
            log.info("Falling back to mock test data");
            return getMockTestData();
        }
    }

    public SpeechAnalysisResponse analyzeQuestionResponse(String question, String base64Audio, String audioFormat, String section) {
        try {
            log.info("Analyzing response for section: {}", section);
            
            // Step 1: Transcribe audio to text
            String transcript = speechToTextService.transcribeAudio(base64Audio, audioFormat);
            log.info("Transcription completed for section {}: {}", section, transcript);
            
            // Step 2: Analyze with DeepSeek AI
            SpeechAnalysisResponse analysis = deepSeekAnalysisService.analyzeSpeech(question, transcript);
            log.info("Analysis completed for section {} with overall score: {}", section, analysis.getOverallScore());
            
            return analysis;

        } catch (Exception e) {
            log.error("Error analyzing question response: {}", e.getMessage());
            return createMockAnalysis(question, "Error occurred during analysis");
        }
    }

    public Map<String, Object> getTestSummary(List<SpeechAnalysisResponse> allAnalyses) {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            if (allAnalyses == null || allAnalyses.isEmpty()) {
                return summary;
            }

            // Calculate overall statistics
            double overallAverage = allAnalyses.stream()
                .mapToDouble(analysis -> analysis.getOverallScore() != null ? analysis.getOverallScore() : 0.0)
                .average()
                .orElse(0.0);

            double fluencyAverage = allAnalyses.stream()
                .mapToDouble(analysis -> analysis.getFluencyScore() != null ? analysis.getFluencyScore() : 0.0)
                .average()
                .orElse(0.0);

            double lexicalAverage = allAnalyses.stream()
                .mapToDouble(analysis -> analysis.getLexicalScore() != null ? analysis.getLexicalScore() : 0.0)
                .average()
                .orElse(0.0);

            double grammaticalAverage = allAnalyses.stream()
                .mapToDouble(analysis -> analysis.getGrammaticalScore() != null ? analysis.getGrammaticalScore() : 0.0)
                .average()
                .orElse(0.0);

            double pronunciationAverage = allAnalyses.stream()
                .mapToDouble(analysis -> analysis.getPronunciationScore() != null ? analysis.getPronunciationScore() : 0.0)
                .average()
                .orElse(0.0);

            // Determine overall band score
            String overallBand = determineBandScore(overallAverage);

            summary.put("overallScore", Math.round(overallAverage * 10.0) / 10.0);
            summary.put("overallBand", overallBand);
            summary.put("fluencyScore", Math.round(fluencyAverage * 10.0) / 10.0);
            summary.put("lexicalScore", Math.round(lexicalAverage * 10.0) / 10.0);
            summary.put("grammaticalScore", Math.round(grammaticalAverage * 10.0) / 10.0);
            summary.put("pronunciationScore", Math.round(pronunciationAverage * 10.0) / 10.0);
            summary.put("totalQuestions", allAnalyses.size());
            summary.put("completedAt", new Date());

            // Generate overall feedback
            String overallFeedback = generateOverallFeedback(overallAverage, fluencyAverage, lexicalAverage, grammaticalAverage, pronunciationAverage);
            summary.put("overallFeedback", overallFeedback);

            log.info("Test summary generated with overall score: {}", overallAverage);
            return summary;

        } catch (Exception e) {
            log.error("Error generating test summary: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private String determineBandScore(double score) {
        if (score >= 8.5) return "9.0";
        if (score >= 8.0) return "8.5";
        if (score >= 7.5) return "8.0";
        if (score >= 7.0) return "7.5";
        if (score >= 6.5) return "7.0";
        if (score >= 6.0) return "6.5";
        if (score >= 5.5) return "6.0";
        if (score >= 5.0) return "5.5";
        if (score >= 4.5) return "5.0";
        if (score >= 4.0) return "4.5";
        if (score >= 3.5) return "4.0";
        if (score >= 3.0) return "3.5";
        return "3.0";
    }

    private String generateOverallFeedback(double overall, double fluency, double lexical, double grammatical, double pronunciation) {
        StringBuilder feedback = new StringBuilder();
        
        feedback.append("Overall, you performed ");
        
        if (overall >= 8.0) {
            feedback.append("excellently in this IELTS speaking test. ");
        } else if (overall >= 7.0) {
            feedback.append("very well in this IELTS speaking test. ");
        } else if (overall >= 6.0) {
            feedback.append("adequately in this IELTS speaking test. ");
        } else {
            feedback.append("below the expected level for this IELTS speaking test. ");
        }

        // Add specific feedback for each component
        if (fluency < 6.0) {
            feedback.append("Work on improving your fluency and coherence. ");
        }
        if (lexical < 6.0) {
            feedback.append("Expand your vocabulary range and accuracy. ");
        }
        if (grammatical < 6.0) {
            feedback.append("Focus on grammatical accuracy and range. ");
        }
        if (pronunciation < 6.0) {
            feedback.append("Practice pronunciation and intonation. ");
        }

        feedback.append("Continue practicing regularly to improve your speaking skills.");
        
        return feedback.toString();
    }

    private Map<String, Object> getMockTestData() {
        Map<String, Object> testData = new HashMap<>();
        List<Map<String, Object>> questions = new ArrayList<>();

        // Mock Part 1
        Map<String, Object> part1 = new HashMap<>();
        part1.put("section", "part1");
        part1.put("question", "Tell me about your hometown. Where is it located and what do you like most about it?");
        part1.put("description", "Introduction & Interview - Questions about familiar topics");
        part1.put("duration", "4-5 minutes");
        part1.put("maxRecordingTime", 60);
        part1.put("order", 1);
        questions.add(part1);

        // Mock Part 2
        Map<String, Object> part2 = new HashMap<>();
        part2.put("section", "part2");
        part2.put("question", "Describe a place you would like to visit. You should say: - where this place is - what you would do there - why you want to visit this place - and explain how you would feel about visiting this place");
        part2.put("description", "Individual Long Turn - Speak for 1-2 minutes on a specific topic");
        part2.put("duration", "3-4 minutes");
        part2.put("maxRecordingTime", 120);
        part2.put("order", 2);
        questions.add(part2);

        // Mock Part 3
        Map<String, Object> part3 = new HashMap<>();
        part3.put("section", "part3");
        part3.put("question", "Why do you think people enjoy traveling to different countries?");
        part3.put("description", "Two-Way Discussion - Deeper discussion on abstract topics");
        part3.put("duration", "4-5 minutes");
        part3.put("maxRecordingTime", 60);
        part3.put("order", 3);
        questions.add(part3);

        testData.put("questions", questions);
        testData.put("totalQuestions", questions.size());
        testData.put("estimatedDuration", "11-14 minutes");
        testData.put("testId", UUID.randomUUID().toString());
        testData.put("createdAt", new Date());

        return testData;
    }

    private SpeechAnalysisResponse createMockAnalysis(String question, String transcript) {
        SpeechAnalysisResponse response = new SpeechAnalysisResponse();
        response.setTranscript(transcript);
        response.setFeedback("Mock analysis - please try again with real audio.");
        response.setOverallScore(6.0);
        response.setFluencyScore(6.0);
        response.setLexicalScore(6.0);
        response.setGrammaticalScore(6.0);
        response.setPronunciationScore(6.0);
        response.setSentenceCorrections(new ArrayList<>());
        response.setVocabularySuggestions(new ArrayList<>());
        response.setPronunciationTips(new ArrayList<>());
        return response;
    }
}
