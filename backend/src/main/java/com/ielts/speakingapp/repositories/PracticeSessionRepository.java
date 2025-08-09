package com.ielts.speakingapp.repositories;

import com.ielts.speakingapp.models.PracticeSession;
import com.ielts.speakingapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
    
    List<PracticeSession> findByUserOrderByCreatedAtDesc(User user);
    
    List<PracticeSession> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
        User user, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT AVG(p.overallScore) FROM PracticeSession p WHERE p.user = :user")
    Double findAverageScoreByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(p) FROM PracticeSession p WHERE p.user = :user")
    Long countSessionsByUser(@Param("user") User user);
    
    @Query("SELECT SUM(p.durationSeconds) FROM PracticeSession p WHERE p.user = :user")
    Long sumDurationByUser(@Param("user") User user);
    
    @Query("SELECT p FROM PracticeSession p WHERE p.user = :user AND p.createdAt >= :startDate ORDER BY p.createdAt DESC")
    List<PracticeSession> findRecentSessionsByUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    List<PracticeSession> findByUserAndTestSectionOrderByCreatedAtDesc(User user, String testSection);
}
