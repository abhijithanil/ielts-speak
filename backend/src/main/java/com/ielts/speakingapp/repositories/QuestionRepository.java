package com.ielts.speakingapp.repositories;

import com.ielts.speakingapp.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findBySectionAndQuestionTypeAndIsActiveTrue(String section, String questionType);
    
    @Query("SELECT q FROM Question q WHERE q.section = :section AND q.questionType = :questionType AND q.isActive = true " +
           "AND q.id NOT IN (SELECT DISTINCT h.question.id FROM UserQuestionHistory h WHERE h.user.id = :userId " +
           "AND h.answeredAt >= :cutoffDate) ORDER BY q.usageCount ASC, q.lastUsed ASC NULLS FIRST")
    List<Question> findUnusedQuestionsForUser(@Param("section") String section, 
                                             @Param("questionType") String questionType, 
                                             @Param("userId") Long userId, 
                                             @Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT q FROM Question q WHERE q.section = :section AND q.questionType = :questionType AND q.isActive = true " +
           "ORDER BY q.usageCount ASC, q.lastUsed ASC NULLS FIRST")
    List<Question> findQuestionsBySectionAndTypeOrderedByUsage(@Param("section") String section, 
                                                              @Param("questionType") String questionType);
    
    @Query("SELECT COUNT(h) FROM UserQuestionHistory h WHERE h.user.id = :userId AND h.question.id = :questionId " +
           "AND h.answeredAt >= :cutoffDate")
    Long countRecentUsageByUser(@Param("userId") Long userId, 
                               @Param("questionId") Long questionId, 
                               @Param("cutoffDate") LocalDateTime cutoffDate);
    
    Optional<Question> findByQuestionTextAndSection(String questionText, String section);
    
    @Query("SELECT q FROM Question q WHERE q.section = :section AND q.isActive = true ORDER BY RANDOM() LIMIT 1")
    Optional<Question> findRandomQuestionBySection(@Param("section") String section);
    
    // Part 3 related questions methods
    @Query("SELECT DISTINCT q.questionGroup FROM Question q WHERE q.section = 'part3' AND q.isActive = true AND q.questionGroup IS NOT NULL")
    List<String> findPart3QuestionGroups();
    
    @Query("SELECT q FROM Question q WHERE q.section = 'part3' AND q.questionGroup = :questionGroup AND q.isActive = true " +
           "ORDER BY q.questionOrder ASC")
    List<Question> findPart3QuestionsByGroup(@Param("questionGroup") String questionGroup);
    
    @Query("SELECT q FROM Question q WHERE q.section = 'part3' AND q.isActive = true " +
           "AND q.id NOT IN (SELECT DISTINCT h.question.id FROM UserQuestionHistory h WHERE h.user.id = :userId " +
           "AND h.answeredAt >= :cutoffDate) " +
           "ORDER BY q.questionGroup ASC, q.questionOrder ASC")
    List<Question> findUnusedPart3QuestionsForUser(@Param("userId") Long userId, 
                                                   @Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT q FROM Question q WHERE q.section = 'part3' AND q.isActive = true " +
           "ORDER BY q.questionGroup ASC, q.questionOrder ASC")
    List<Question> findAllPart3QuestionsOrdered();
}
