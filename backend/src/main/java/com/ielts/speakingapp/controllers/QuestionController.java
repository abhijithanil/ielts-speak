package com.ielts.speakingapp.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ielts.speakingapp.models.Question;
import com.ielts.speakingapp.models.User;
import com.ielts.speakingapp.services.DeepSeekService;
import com.ielts.speakingapp.services.QuestionService;
import com.ielts.speakingapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/questions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class QuestionController {

    @Autowired
    private DeepSeekService deepSeekService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private UserService userService;

    private final List<String> fallbackQuestions = Arrays.asList(
        "Tell me about your hometown. Where is your hometown? What do you like most about it?",
        "Describe your family. How many people are in your family? What do they do?",
        "What do you do for work or study? Do you enjoy it?",
        "Tell me about your hobbies. What do you like to do in your free time?",
        "Describe a place you would like to visit. You should say: - where this place is - what you would do there - why you want to visit this place - and explain how you would feel about visiting this place",
        "Why do you think people enjoy traveling to different countries?",
        "What are the advantages and disadvantages of living in a big city?",
        "How has technology changed the way people communicate in recent years?"
    );

    @GetMapping("/random")
    public Object getRandomQuestion() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return fallbackQuestions.get(new Random().nextInt(fallbackQuestions.size()));
            }
            
            String[] sections = {"part1", "part2", "part3"};
            String randomSection = sections[new Random().nextInt(sections.length)];
            
            List<Map<String, Object>> questions = questionService.getQuestionsForSection(randomSection, currentUser);
            if (!questions.isEmpty()) {
                return questions.get(0);
            }
            
            // Fallback to DeepSeek API if no questions in database
            JsonNode result = deepSeekService.generateQuestion(randomSection);
            if (result.has("questions") && result.get("questions").isArray() && result.get("questions").size() > 0) {
                JsonNode firstQuestion = result.get("questions").get(0);
                if (firstQuestion.has("question")) {
                    return firstQuestion.get("question").asText();
                }
            }
            
            if (result.has("content")) {
                return result.get("content").asText();
            }
            
            return result.toString();
        } catch (Exception e) {
            return fallbackQuestions.get(new Random().nextInt(fallbackQuestions.size()));
        }
    }

    @GetMapping("/random/{section}")
    public Object getRandomQuestionBySection(@PathVariable String section) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return fallbackQuestions.get(new Random().nextInt(fallbackQuestions.size()));
            }
            
            List<Map<String, Object>> questions = questionService.getQuestionsForSection(section, currentUser);
            if (!questions.isEmpty()) {
                return questions.get(0);
            }
            
            // Fallback to DeepSeek API
            JsonNode result = deepSeekService.generateQuestion(section);
            if (result.has("questions") && result.get("questions").isArray() && result.get("questions").size() > 0) {
                JsonNode firstQuestion = result.get("questions").get(0);
                if (firstQuestion.has("question")) {
                    return firstQuestion.get("question").asText();
                }
            }
            
            if (result.has("content")) {
                return result.get("content").asText();
            }
            
            return result.toString();
        } catch (Exception e) {
            return fallbackQuestions.get(new Random().nextInt(fallbackQuestions.size()));
        }
    }

    @GetMapping("/section/{section}")
    public Map<String, Object> getAllQuestionsForSection(@PathVariable String section) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return getFallbackSectionResponse(section);
            }
            
            List<Map<String, Object>> questions = questionService.getQuestionsForSection(section, currentUser);
            if (!questions.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("section", section);
                
                if ("part2".equalsIgnoreCase(section)) {
                    // For Part 2, return the first cue card
                    Map<String, Object> firstQuestion = questions.get(0);
                    response.put("type", "cue_card");
                    response.put("topic", firstQuestion.get("topic"));
                    response.put("bulletPoints", firstQuestion.get("bulletPoints"));
                    response.put("sampleAnswer", firstQuestion.get("sampleAnswer"));
                } else {
                    // For Part 1 and Part 3, combine all questions
                    response.put("type", "questions");
                    List<String> allQuestions = new ArrayList<>();
                    for (Map<String, Object> questionObj : questions) {
                        if (questionObj.get("questions") instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> questionList = (List<String>) questionObj.get("questions");
                            allQuestions.addAll(questionList);
                        }
                    }
                    response.put("questions", allQuestions);
                }
                
                return response;
            }
            
            return getFallbackSectionResponse(section);
        } catch (Exception e) {
            return getFallbackSectionResponse(section);
        }
    }

    @GetMapping("/complete-test")
    public Map<String, Object> getCompleteTest() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return getFallbackCompleteTest();
            }
            
            return questionService.getCompleteTestQuestions(currentUser);
        } catch (Exception e) {
            return getFallbackCompleteTest();
        }
    }

    @PostMapping("/record-usage")
    public ResponseEntity<Map<String, Object>> recordQuestionUsage(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            
            String questionText = (String) request.get("questionText");
            String section = (String) request.get("section");
            String practiceSessionId = (String) request.get("practiceSessionId");
            String testSection = (String) request.get("testSection");
            
            if (questionText == null || section == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }
            
            // Find the question in database
            Question question = questionService.saveQuestion(section, questionText, 
                "part2".equalsIgnoreCase(section) ? "cue_card" : "questions", 
                null, null, null);
            
            if (question != null) {
                questionService.recordQuestionUsage(currentUser, question, practiceSessionId, testSection);
                return ResponseEntity.ok(Map.of("success", true, "message", "Question usage recorded"));
            }
            
            return ResponseEntity.ok(Map.of("success", false, "message", "Question not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public List<String> getAllQuestions() {
        return fallbackQuestions;
    }

    @GetMapping("/count")
    public int getQuestionCount() {
        return fallbackQuestions.size();
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                return userService.findByUsername(username);
            }
        } catch (Exception e) {
            // Log error but don't throw
        }
        return null;
    }

    private Map<String, Object> getFallbackSectionResponse(String section) {
        Map<String, Object> response = new HashMap<>();
        response.put("section", section);
        
        if (section.equalsIgnoreCase("part2")) {
            response.put("type", "cue_card");
            response.put("topic", "Describe a memorable trip you have taken");
            response.put("bulletPoints", Arrays.asList(
                "Where did you go?",
                "Who did you travel with?",
                "What did you do there?",
                "Why was it memorable?"
            ));
        } else {
            response.put("type", "questions");
            if (section.equalsIgnoreCase("part1")) {
                response.put("questions", Arrays.asList(
                    "Tell me about your hometown. Where is your hometown?",
                    "What do you do for work or study?",
                    "Do you enjoy reading books? What kind of books do you prefer?",
                    "How do you usually spend your weekends?"
                ));
            } else {
                response.put("questions", Arrays.asList(
                    "Why do you think people enjoy traveling to different countries?",
                    "What are the advantages and disadvantages of traveling alone?",
                    "How has tourism changed in recent years?",
                    "What impact does tourism have on local communities?"
                ));
            }
        }
        
        return response;
    }

    private Map<String, Object> getFallbackCompleteTest() {
        Map<String, Object> completeTest = new HashMap<>();
        
        // Part 1
        Map<String, Object> part1 = new HashMap<>();
        part1.put("section", "part1");
        part1.put("type", "questions");
        part1.put("questions", Arrays.asList(
            "Tell me about your hometown. Where is your hometown?",
            "What do you do for work or study?",
            "Do you enjoy reading books? What kind of books do you prefer?",
            "How do you usually spend your weekends?"
        ));
        completeTest.put("part1", part1);
        
        // Part 2
        Map<String, Object> part2 = new HashMap<>();
        part2.put("section", "part2");
        part2.put("type", "cue_card");
        part2.put("topic", "Describe a memorable trip you have taken");
        part2.put("bulletPoints", Arrays.asList(
            "Where did you go?",
            "Who did you travel with?",
            "What did you do there?",
            "Why was it memorable?"
        ));
        completeTest.put("part2", part2);
        
        // Part 3
        Map<String, Object> part3 = new HashMap<>();
        part3.put("section", "part3");
        part3.put("type", "questions");
        part3.put("questions", Arrays.asList(
            "Why do you think people enjoy traveling to different countries?",
            "What are the advantages and disadvantages of traveling alone?",
            "How has tourism changed in recent years?",
            "What impact does tourism have on local communities?"
        ));
        completeTest.put("part3", part3);
        
        return completeTest;
    }
}
