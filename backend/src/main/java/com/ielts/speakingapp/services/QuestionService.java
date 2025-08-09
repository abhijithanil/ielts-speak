package com.ielts.speakingapp.services;

import com.ielts.speakingapp.models.Question;
import com.ielts.speakingapp.models.User;
import com.ielts.speakingapp.models.UserQuestionHistory;
import com.ielts.speakingapp.repositories.QuestionRepository;
import com.ielts.speakingapp.repositories.UserQuestionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserQuestionHistoryRepository userQuestionHistoryRepository;
    private final DeepSeekService deepSeekService;

    @Value("${app.use-deepseek-api:false}")
    private boolean useDeepSeekApi;

    @Value("${app.question-rotation-days:50}")
    private int questionRotationDays;

    /**
     * Get questions for a specific section, avoiding recently used questions
     */
    public List<Map<String, Object>> getQuestionsForSection(String section, User user) {
        try {
            if (useDeepSeekApi && Math.random() < 0.1) { // 10% chance to use DeepSeek API
                return getQuestionsFromDeepSeek(section);
            } else {
                return getQuestionsFromDatabase(section, user);
            }
        } catch (Exception e) {
            log.error("Error getting questions for section {}: {}", section, e.getMessage());
            return getFallbackQuestions(section);
        }
    }

    /**
     * Get complete test questions from database
     */
    public Map<String, Object> getCompleteTestQuestions(User user) {
        Map<String, Object> completeTest = new HashMap<>();
        
        try {
            // Get questions for each section
            completeTest.put("part1", getQuestionsForSection("part1", user));
            completeTest.put("part2", getQuestionsForSection("part2", user));
            completeTest.put("part3", getQuestionsForSection("part3", user));
        } catch (Exception e) {
            log.error("Error getting complete test questions: {}", e.getMessage());
            completeTest = getFallbackCompleteTest();
        }
        
        return completeTest;
    }

    /**
     * Get questions from database with rotation
     */
    private List<Map<String, Object>> getQuestionsFromDatabase(String section, User user) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(questionRotationDays);
        
        if ("part1".equalsIgnoreCase(section)) {
            // Get regular questions for Part 1 - limit to 4-6 questions
            List<Question> questions = questionRepository.findUnusedQuestionsForUser(section, "questions", user.getId(), cutoffDate);
            if (questions.isEmpty()) {
                // If no unused questions, get all questions ordered by usage
                questions = questionRepository.findQuestionsBySectionAndTypeOrderedByUsage(section, "questions");
            }
            
            // Limit to 4-6 questions (randomly choose between 4-6)
            int maxQuestions = new Random().nextInt(3) + 4; // 4 to 6 questions
            int questionCount = 0;
            
            List<String> questionsList = new ArrayList<>();
            for (Question question : questions) {
                if (questionCount >= maxQuestions) break;
                questionsList.add(question.getQuestionText());
                questionCount++;
            }
            
            if (!questionsList.isEmpty()) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put("type", "questions");
                questionMap.put("questions", questionsList);
                questionMap.put("section", section);
                result.add(questionMap);
            }
        } else if ("part2".equalsIgnoreCase(section)) {
            // Get cue card questions - limit to 1 question
            List<Question> cueCards = questionRepository.findUnusedQuestionsForUser(section, "cue_card", user.getId(), cutoffDate);
            if (cueCards.isEmpty()) {
                // If no unused questions, get all questions ordered by usage
                cueCards = questionRepository.findQuestionsBySectionAndTypeOrderedByUsage(section, "cue_card");
            }
            
            // Limit to 1 question
            if (!cueCards.isEmpty()) {
                Question question = cueCards.get(0);
                Map<String, Object> cueCard = new HashMap<>();
                cueCard.put("type", "cue_card");
                cueCard.put("topic", question.getTopic());
                cueCard.put("bulletPoints", question.getBulletPoints());
                cueCard.put("sampleAnswer", question.getSampleAnswer());
                cueCard.put("section", section);
                result.add(cueCard);
            }
        } else if ("part3".equalsIgnoreCase(section)) {
            // Get Part 3 related questions - group them by question group and limit to 5-7 questions
            List<Question> part3Questions = questionRepository.findUnusedPart3QuestionsForUser(user.getId(), cutoffDate);
            if (part3Questions.isEmpty()) {
                // If no unused questions, get all questions ordered by group and order
                part3Questions = questionRepository.findAllPart3QuestionsOrdered();
            }
            
            // Group questions by questionGroup
            Map<String, List<Question>> groupedQuestions = new HashMap<>();
            for (Question question : part3Questions) {
                String group = question.getQuestionGroup() != null ? question.getQuestionGroup() : "default";
                groupedQuestions.computeIfAbsent(group, k -> new ArrayList<>()).add(question);
            }
            
            // Select one group randomly and limit to 5-7 questions
            List<String> groupKeys = new ArrayList<>(groupedQuestions.keySet());
            if (!groupKeys.isEmpty()) {
                String selectedGroup = groupKeys.get(new Random().nextInt(groupKeys.size()));
                List<Question> groupQuestions = groupedQuestions.get(selectedGroup);
                
                // Sort by questionOrder if available
                groupQuestions.sort((q1, q2) -> {
                    Integer order1 = q1.getQuestionOrder() != null ? q1.getQuestionOrder() : 0;
                    Integer order2 = q2.getQuestionOrder() != null ? q2.getQuestionOrder() : 0;
                    return order1.compareTo(order2);
                });
                
                // Limit to 5-7 questions
                int maxQuestions = new Random().nextInt(3) + 5; // 5 to 7 questions
                int questionCount = 0;
                
                List<String> questionsList = new ArrayList<>();
                for (Question question : groupQuestions) {
                    if (questionCount >= maxQuestions) break;
                    questionsList.add(question.getQuestionText());
                    questionCount++;
                }
                
                if (!questionsList.isEmpty()) {
                    Map<String, Object> questionGroup = new HashMap<>();
                    questionGroup.put("type", "questions");
                    questionGroup.put("questions", questionsList);
                    questionGroup.put("section", section);
                    questionGroup.put("topic", selectedGroup); // Use group as topic
                    result.add(questionGroup);
                }
            }
        }
        
        return result;
    }

    /**
     * Get questions from DeepSeek API
     */
    private List<Map<String, Object>> getQuestionsFromDeepSeek(String section) {
        // This will call the existing DeepSeek service
        // For now, return fallback questions
        return getFallbackQuestions(section);
    }

    /**
     * Save a question to the database
     */
    public Question saveQuestion(String section, String questionText, String questionType, 
                               String topic, List<String> bulletPoints, String sampleAnswer) {
        try {
            // Check if question already exists
            Optional<Question> existingQuestion = questionRepository.findByQuestionTextAndSection(questionText, section);
            if (existingQuestion.isPresent()) {
                log.info("Question already exists in database: {}", questionText);
                return existingQuestion.get();
            }
            
            Question question = new Question();
            question.setSection(section);
            question.setQuestionText(questionText);
            question.setQuestionType(questionType);
            question.setTopic(topic);
            question.setBulletPoints(bulletPoints);
            question.setSampleAnswer(sampleAnswer);
            question.setIsActive(true);
            question.setUsageCount(0);
            
            Question savedQuestion = questionRepository.save(question);
            log.info("Saved new question to database: {}", questionText);
            return savedQuestion;
        } catch (Exception e) {
            log.error("Error saving question to database: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Save a question to the database with additional fields for Part 3
     */
    public Question saveQuestion(String section, String questionText, String questionType, 
                               String topic, List<String> bulletPoints, String sampleAnswer,
                               String questionGroup, Integer questionOrder) {
        try {
            // Check if question already exists
            Optional<Question> existingQuestion = questionRepository.findByQuestionTextAndSection(questionText, section);
            if (existingQuestion.isPresent()) {
                log.info("Question already exists in database: {}", questionText);
                return existingQuestion.get();
            }
            
            Question question = new Question();
            question.setSection(section);
            question.setQuestionText(questionText);
            question.setQuestionType(questionType);
            question.setTopic(topic);
            question.setBulletPoints(bulletPoints);
            question.setSampleAnswer(sampleAnswer);
            question.setQuestionGroup(questionGroup);
            question.setQuestionOrder(questionOrder);
            question.setIsActive(true);
            question.setUsageCount(0);
            
            Question savedQuestion = questionRepository.save(question);
            log.info("Saved new question to database: {}", questionText);
            return savedQuestion;
        } catch (Exception e) {
            log.error("Error saving question to database: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Record that a user has answered a question
     */
    public void recordQuestionUsage(User user, Question question, String practiceSessionId, String testSection) {
        try {
            UserQuestionHistory history = new UserQuestionHistory();
            history.setUser(user);
            history.setQuestion(question);
            history.setPracticeSessionId(practiceSessionId);
            history.setTestSection(testSection);
            
            userQuestionHistoryRepository.save(history);
            
            // Update question usage count and last used
            question.setUsageCount(question.getUsageCount() + 1);
            question.setLastUsed(LocalDateTime.now());
            questionRepository.save(question);
            
            log.info("Recorded question usage for user {}: question {}", user.getUsername(), question.getId());
        } catch (Exception e) {
            log.error("Error recording question usage: {}", e.getMessage());
        }
    }

    /**
     * Initialize database with default questions if empty
     */
    public void initializeDefaultQuestions() {
        try {
            long questionCount = questionRepository.count();
            if (questionCount == 0) {
                log.info("Initializing database with default questions");
                initializePart1Questions();
                initializePart2Questions();
                initializePart3Questions();
            }
        } catch (Exception e) {
            log.error("Error initializing default questions: {}", e.getMessage());
        }
    }

    private void initializePart1Questions() {
        List<String> part1Questions = Arrays.asList(
            "Tell me about your hometown. Where is your hometown?",
            "What do you do for work or study?",
            "Do you enjoy reading books? What kind of books do you prefer?",
            "How do you usually spend your weekends?",
            "Describe your family. How many people are in your family?",
            "What do you like to do in your free time?",
            "Do you prefer living in a city or countryside?",
            "What kind of music do you enjoy listening to?"
        );
        
        for (String questionText : part1Questions) {
            saveQuestion("part1", questionText, "questions", null, null, null);
        }
    }

    private void initializePart2Questions() {
        List<Map<String, Object>> part2Questions = Arrays.asList(
            Map.of(
                "topic", "Describe a memorable trip you have taken",
                "bulletPoints", Arrays.asList("Where did you go?", "Who did you travel with?", "What did you do there?", "Why was it memorable?")
            ),
            Map.of(
                "topic", "Describe a book you enjoyed reading",
                "bulletPoints", Arrays.asList("What book was it?", "Why did you like it?", "When did you read it?", "Would you recommend it to others?")
            ),
            Map.of(
                "topic", "Describe a person who has influenced you",
                "bulletPoints", Arrays.asList("Who is this person?", "How do you know them?", "How have they influenced you?", "Why are they important to you?")
            ),
            Map.of(
                "topic", "Describe a place you would like to visit",
                "bulletPoints", Arrays.asList("Where is this place?", "Why do you want to visit it?", "What would you do there?", "How would you feel about visiting this place?")
            )
        );
        
        for (Map<String, Object> questionData : part2Questions) {
            String topic = (String) questionData.get("topic");
            @SuppressWarnings("unchecked")
            List<String> bulletPoints = (List<String>) questionData.get("bulletPoints");
            saveQuestion("part2", topic, "cue_card", topic, bulletPoints, null);
        }
    }

    private void initializePart3Questions() {
        // Define Part 3 question groups - each group contains related questions
        List<Map<String, Object>> part3QuestionGroups = Arrays.asList(
            // Travel and Tourism Group
            Map.of(
                "group", "travel_tourism",
                "topic", "Travel and Tourism",
                "questions", Arrays.asList(
                    "Why do you think people enjoy traveling to different countries?",
                    "What are the advantages and disadvantages of traveling alone?",
                    "How has tourism changed in recent years?",
                    "What impact does tourism have on local communities?"
                )
            ),
            // Technology and Communication Group
            Map.of(
                "group", "technology_communication",
                "topic", "Technology and Communication",
                "questions", Arrays.asList(
                    "How has technology changed the way people communicate?",
                    "What are the advantages and disadvantages of social media?",
                    "Do you think technology has made people more or less social?",
                    "How do you think communication will change in the future?"
                )
            ),
            // Education and Learning Group
            Map.of(
                "group", "education_learning",
                "topic", "Education and Learning",
                "questions", Arrays.asList(
                    "What are the benefits of learning a foreign language?",
                    "How do you think education will change in the future?",
                    "What are the advantages and disadvantages of online learning?",
                    "Do you think traditional classroom learning is still important?"
                )
            ),
            // Urban Living Group
            Map.of(
                "group", "urban_living",
                "topic", "Urban Living and Cities",
                "questions", Arrays.asList(
                    "What are the advantages and disadvantages of living in a big city?",
                    "How do you think cities will change in the future?",
                    "What makes a city a good place to live?",
                    "Do you think people should be encouraged to live in cities or rural areas?"
                )
            ),
            // Environment and Sustainability Group
            Map.of(
                "group", "environment_sustainability",
                "topic", "Environment and Sustainability",
                "questions", Arrays.asList(
                    "What environmental issues concern you the most?",
                    "How can individuals contribute to environmental protection?",
                    "Do you think governments should do more to protect the environment?",
                    "What changes would you like to see in how we use natural resources?"
                )
            )
        );
        
        for (Map<String, Object> groupData : part3QuestionGroups) {
            String group = (String) groupData.get("group");
            String topic = (String) groupData.get("topic");
            @SuppressWarnings("unchecked")
            List<String> questions = (List<String>) groupData.get("questions");
            
            for (int i = 0; i < questions.size(); i++) {
                saveQuestion("part3", questions.get(i), "questions", topic, null, null, group, i + 1);
            }
        }
    }

    private List<Map<String, Object>> getFallbackQuestions(String section) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if ("part1".equalsIgnoreCase(section)) {
            // Part 1: 4-6 questions
            List<String> part1Questions = Arrays.asList(
                "Tell me about your hometown. Where is your hometown?",
                "What do you do for work or study?",
                "Do you enjoy reading books? What kind of books do you prefer?",
                "How do you usually spend your weekends?",
                "Describe your family. How many people are in your family?",
                "What do you like to do in your free time?"
            );
            
            // Randomly select 4-6 questions
            int maxQuestions = new Random().nextInt(3) + 4; // 4 to 6 questions
            Collections.shuffle(part1Questions);
            
            List<String> selectedQuestions = part1Questions.subList(0, Math.min(maxQuestions, part1Questions.size()));
            
            Map<String, Object> questions = new HashMap<>();
            questions.put("type", "questions");
            questions.put("questions", selectedQuestions);
            questions.put("section", section);
            result.add(questions);
            
        } else if ("part2".equalsIgnoreCase(section)) {
            // Part 2: 1 question (cue card)
            Map<String, Object> cueCard = new HashMap<>();
            cueCard.put("type", "cue_card");
            cueCard.put("topic", "Describe a memorable trip you have taken");
            cueCard.put("bulletPoints", Arrays.asList(
                "Where did you go?",
                "Who did you travel with?",
                "What did you do there?",
                "Why was it memorable?"
            ));
            cueCard.put("section", section);
            result.add(cueCard);
            
        } else if ("part3".equalsIgnoreCase(section)) {
            // Part 3: 5-7 questions (related group)
            List<String> part3Questions = Arrays.asList(
                "Why do you think people enjoy traveling to different countries?",
                "What are the advantages and disadvantages of traveling alone?",
                "How has tourism changed in recent years?",
                "What impact does tourism have on local communities?",
                "What are the benefits of learning a foreign language?",
                "How has technology changed the way people communicate?",
                "What are the advantages and disadvantages of living in a big city?"
            );
            
            // Randomly select 5-7 questions
            int maxQuestions = new Random().nextInt(3) + 5; // 5 to 7 questions
            Collections.shuffle(part3Questions);
            
            List<String> selectedQuestions = part3Questions.subList(0, Math.min(maxQuestions, part3Questions.size()));
            
            Map<String, Object> questions = new HashMap<>();
            questions.put("type", "questions");
            questions.put("questions", selectedQuestions);
            questions.put("section", section);
            questions.put("topic", "Travel and Tourism"); // Default topic for fallback
            result.add(questions);
        }
        
        return result;
    }

    private Map<String, Object> getFallbackCompleteTest() {
        Map<String, Object> completeTest = new HashMap<>();
        completeTest.put("part1", getFallbackQuestions("part1"));
        completeTest.put("part2", getFallbackQuestions("part2"));
        completeTest.put("part3", getFallbackQuestions("part3"));
        return completeTest;
    }
}
