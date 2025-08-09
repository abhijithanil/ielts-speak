package com.ielts.speakingapp.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class DeepSeekService {

    @Qualifier("getDeepSeekApiKey")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_TOKENS = 2000;

    // Updated to return questions + sample answers
    public JsonNode generateQuestion(String section) {
        String prompt = buildQuestionPrompt(section);
        return callDeepSeekAPI(prompt);
    }

    public String analyzeSpeech(String question, String transcript) {
        String prompt = buildAnalysisPrompt(question, transcript);
        JsonNode result = callDeepSeekAPI(prompt);
        
        // Convert JsonNode to String for backward compatibility
        if (result.has("content")) {
            return result.get("content").asText();
        }
        return result.toString();
    }

    public String generateFullIeltsTest() {
        log.info("Generating full IELTS Speaking test");
        String prompt = buildFullTestPrompt();
        JsonNode result = callDeepSeekAPI(prompt);
        
        // Convert JsonNode to String for backward compatibility
        String response = result.toString();
        
        // Log the response and save to file
        log.info("Full IELTS test generated successfully");
        log.debug("Generated test content: {}", response);
        
        // Save to file for verification
        saveToFile(response, "ielts_test_generation");
        
        return response;
    }

    // Updated prompt engineering for multiple Q&A
    private String buildQuestionPrompt(String section) {
        switch (section.toLowerCase()) {
            case "part1":
                return """
                    Generate 4 IELTS Speaking Part 1 questions with sample answers (Band 7-9 level). 
                    Return in strict JSON format:
                    {
                      "questions": [
                        {
                          "question": "What do you do for work?",
                          "sampleAnswer": "I'm currently working as a software engineer at a tech company. I develop applications and troubleshoot code, which I find quite rewarding."
                        },
                        {
                          "question": "Do you enjoy your job?",
                          "sampleAnswer": "Yes, I genuinely do. While it can be challenging at times, solving problems and creating useful software is very satisfying."
                        }
                        // Add 2 more Q&A pairs...
                      ]
                    }
                    Topics: work, studies, hobbies, hometown, family, daily life.
                    """;
            case "part2":
                return """
                    Generate 1 IELTS Speaking Part 2 cue card with 3-4 bullet points.
                    Return in strict JSON format:
                    {
                      "topic": "Describe a book you enjoyed",
                      "bullet_points": [
                        "What book was it?",
                        "Why did you like it?",
                        "Would you recommend it to others?"
                      ],
                      "sampleAnswer": "One book I really enjoyed was 'Atomic Habits' by James Clear..."
                    }
                    """;
            case "part3":
                return """
                    Generate 4 IELTS Speaking Part 3 discussion questions with sample answers (Band 7-9 level).
                    Return in strict JSON format:
                    {
                      "questions": [
                        {
                          "question": "Why do you think reading is important?",
                          "sampleAnswer": "Reading is crucial because it expands knowledge, improves vocabulary, and enhances critical thinking skills..."
                        },
                        {
                          "question": "How has digital technology changed reading habits?",
                          "sampleAnswer": "Digital technology has made books more accessible through e-readers, but some argue it has reduced attention spans..."
                        }
                        // Add 2 more Q&A pairs...
                      ]
                    }
                    """;
            default:
                return "Generate an IELTS Speaking question with a sample answer in JSON format.";
        }
    }

    private String buildFullTestPrompt() {
        return """
            Generate a full IELTS Speaking mock test with 3 parts (Part 1, Part 2, Part 3) in **strict JSON format**. Follow this exact structure:
            
            {
              "part1": {
                "questions": ["Question 1", "Question 2", "Question 3", "Question 4"]
              },
              "part2": {
                "topic": "Describe a memorable event",
                "bullet_points": ["What happened?", "Who was involved?", "Why was it memorable?"]
              },
              "part3": {
                "discussion_questions": ["Question 1", "Question 2", "Question 3"]
              }
            }
            
            Instructions:
            
            Part 1: Ask 4-5 personal questions (e.g., work, hobbies, hometown, family, studies, daily routine).
            
            Part 2: Provide 1 topic + 3-4 bullet points (e.g., a trip, skill, book, person, place, or event).
            
            Part 3: Ask 4-5 deeper follow-up questions related to Part 2 topic.
            
            Ensure all questions mimic the real IELTS Speaking test format and difficulty level.
            
            Output MUST be valid JSON only (no extra text or comments).
            """;
    }

    private String buildAnalysisPrompt(String question, String transcript) {
        return String.format("""
            Analyze this IELTS speaking response and provide detailed feedback in JSON format.
            
            Question: %s
            Transcript: %s
            
            Provide feedback in this exact JSON format:
            {
                "transcript": "the transcript text",
                "feedback": "overall feedback paragraph",
                "overallScore": 7.5,
                "fluencyScore": 7.0,
                "lexicalScore": 8.0,
                "grammaticalScore": 7.5,
                "pronunciationScore": 7.0,
                "sentenceCorrections": [
                    {"original": "I am go", "corrected": "I am going", "explanation": "grammar error"}
                ],
                "vocabularySuggestions": [
                    {"word": "good", "suggestion": "excellent", "context": "for better vocabulary"}
                ],
                "pronunciationTips": [
                    {"word": "pronunciation", "tip": "stress on second syllable"}
                ]
            }
            
            Score on a scale of 0-9 (IELTS band scale). Be realistic but encouraging.
            """, question, transcript);
    }

    private JsonNode callDeepSeekAPI(String prompt) {
        try {
            log.debug("Calling DeepSeek API with model: {} and prompt length: {}", MODEL_NAME, prompt.length());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("temperature", TEMPERATURE);
            requestBody.put("max_tokens", MAX_TOKENS);
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.set("messages", messages);

            String requestBodyString = requestBody.toString();
            log.debug("DeepSeek API request body: {}", requestBodyString);
            
            // Save request to file for verification
            saveToFile(requestBodyString, "deepseek_request");

            HttpEntity<String> request = new HttpEntity<>(requestBodyString, headers);

            ResponseEntity<String> response = restTemplate.exchange(DEEPSEEK_API_URL, HttpMethod.POST, request, String.class);

            log.debug("DeepSeek API response status: {}", response.getStatusCode());
            log.debug("DeepSeek API response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Save response to file for verification
                saveToFile(response.getBody(), "deepseek_response");
                
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode choices = responseJson.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode choice = choices.get(0);
                    JsonNode messageNode = choice.get("message");
                    if (messageNode != null) {
                        JsonNode content = messageNode.get("content");
                        if (content != null) {
                            String result = content.asText().trim();
                            log.info("Successfully extracted content from DeepSeek {} response", MODEL_NAME);
                            
                            // Clean the response - remove markdown code blocks and backticks
                            String cleanedResult = cleanJsonResponse(result);
                            
                            // Try to parse as JSON, if it fails, return as string wrapped in JsonNode
                            try {
                                return objectMapper.readTree(cleanedResult);
                            } catch (Exception e) {
                                log.warn("Response is not valid JSON after cleaning, returning as string: {}", e.getMessage());
                                ObjectNode wrapper = objectMapper.createObjectNode();
                                wrapper.put("content", cleanedResult);
                                return wrapper;
                            }
                        }
                    }
                }
            }

            log.warn("Failed to extract content from DeepSeek response, using mock response");
            return getMockResponse(prompt);
        } catch (Exception e) {
            log.error("Error calling DeepSeek API: {}", e.getMessage(), e);
            return getMockResponse(prompt);
        }
    }

    /**
     * Clean JSON response by removing markdown code blocks and backticks
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return response;
        }
        
        String cleaned = response.trim();
        
        // Remove markdown code blocks (```json ... ```)
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        // Remove any remaining backticks
        cleaned = cleaned.replaceAll("`", "");
        
        // Remove any leading/trailing whitespace
        cleaned = cleaned.trim();
        
        log.debug("Cleaned JSON response: {}", cleaned);
        return cleaned;
    }

    private void saveToFile(String content, String prefix) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s.json", prefix, timestamp);
            String filepath = Paths.get(System.getProperty("user.dir"), filename).toString();
            
            try (FileWriter writer = new FileWriter(filepath)) {
                writer.write(content);
            }
            
            log.info("Saved {} to file: {}", prefix, filepath);
        } catch (IOException e) {
            log.error("Failed to save {} to file: {}", prefix, e.getMessage());
        }
    }

    // Updated mock responses to include Q&A pairs
    private JsonNode getMockResponse(String prompt) {
        try {
            if (prompt.contains("Part 1")) {
                return objectMapper.readTree("""
                    {
                      "questions": [
                        {
                          "question": "Do you work or study?",
                          "sampleAnswer": "I'm currently a university student majoring in computer science. I'm in my final year."
                        },
                        {
                          "question": "What do you like about your hometown?",
                          "sampleAnswer": "My hometown is quite peaceful with beautiful parks. What I love most is the sense of community there."
                        },
                        {
                          "question": "Do you prefer reading books or watching movies?",
                          "sampleAnswer": "While I enjoy both, I prefer books because they stimulate my imagination more."
                        },
                        {
                          "question": "How do you usually spend your weekends?",
                          "sampleAnswer": "I typically relax by meeting friends, playing sports, and sometimes catching up on studies."
                        }
                      ]
                    }
                    """);
            } else if (prompt.contains("Part 3")) {
                return objectMapper.readTree("""
                    {
                      "questions": [
                        {
                          "question": "Why do you think education is important?",
                          "sampleAnswer": "Education is fundamental as it equips people with knowledge and skills needed for personal and professional growth."
                        },
                        {
                          "question": "How has technology changed education?",
                          "sampleAnswer": "Technology has revolutionized education through online learning platforms, making knowledge more accessible globally."
                        },
                        {
                          "question": "What are the advantages of studying abroad?",
                          "sampleAnswer": "Studying abroad offers exposure to new cultures, improves language skills, and often provides better career opportunities."
                        },
                        {
                          "question": "Do you think traditional classrooms will disappear?",
                          "sampleAnswer": "While online education is growing, traditional classrooms will likely persist for their social interaction and structured environment."
                        }
                      ]
                    }
                    """);
            } else if (prompt.contains("Part 2")) {
                return objectMapper.readTree("""
                    {
                      "topic": "Describe a memorable trip",
                      "bullet_points": [
                        "Where did you go?",
                        "Who were you with?",
                        "What made it special?"
                      ],
                      "sampleAnswer": "Last summer, I visited Bali with my family. What made it unforgettable was the stunning beaches and the warm hospitality of the locals."
                    }
                    """);
            } else if (prompt.contains("full IELTS")) {
                return objectMapper.readTree("""
                    {
                      "part1": {
                        "questions": [
                          "Tell me about your hometown. Where is it located?",
                          "What do you do for work or study?",
                          "Do you enjoy reading books? What kind of books do you prefer?",
                          "How do you usually spend your weekends?"
                        ]
                      },
                      "part2": {
                        "topic": "Describe a memorable trip you have taken",
                        "bullet_points": [
                          "Where did you go?",
                          "Who did you travel with?",
                          "What did you do there?",
                          "Why was it memorable?"
                        ]
                      },
                      "part3": {
                        "discussion_questions": [
                          "Why do you think people enjoy traveling to different countries?",
                          "What are the advantages and disadvantages of traveling alone?",
                          "How has tourism changed in recent years?",
                          "Do you think it's important to learn about other cultures?"
                        ]
                      }
                    }
                    """);
            } else {
                return objectMapper.createObjectNode().put("error", "Invalid section requested");
            }
        } catch (Exception e) {
            log.error("Error creating mock response", e);
            return objectMapper.createObjectNode().put("error", "Failed to generate mock response");
        }
    }
}
