package com.ielts.speakingapp.repositories;

import com.ielts.speakingapp.models.UserQuestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserQuestionHistoryRepository extends JpaRepository<UserQuestionHistory, Long> {
    
    List<UserQuestionHistory> findByUserIdOrderByAnsweredAtDesc(Long userId);
    
    @Query("SELECT h FROM UserQuestionHistory h WHERE h.user.id = :userId AND h.answeredAt >= :cutoffDate")
    List<UserQuestionHistory> findRecentHistoryByUser(@Param("userId") Long userId, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT h.question.id FROM UserQuestionHistory h WHERE h.user.id = :userId AND h.answeredAt >= :cutoffDate")
    List<Long> findRecentQuestionIdsByUser(@Param("userId") Long userId, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    boolean existsByUserIdAndQuestionIdAndAnsweredAtAfter(Long userId, Long questionId, LocalDateTime cutoffDate);
}
