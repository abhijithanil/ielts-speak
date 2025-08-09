package com.ielts.speakingapp.services;

import com.ielts.speakingapp.models.PracticeSession;
import com.ielts.speakingapp.models.User;
import com.ielts.speakingapp.repositories.PracticeSessionRepository;
import com.ielts.speakingapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private PracticeSessionRepository practiceSessionRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getUserDashboardStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PracticeSession> allSessions = practiceSessionRepository.findByUserOrderByCreatedAtDesc(user);
        
        Map<String, Object> stats = new HashMap<>();
        
        // Basic stats
        stats.put("totalSessions", allSessions.size());
        stats.put("averageScore", calculateAverageScore(allSessions));
        stats.put("totalTimeSpent", calculateTotalTimeSpent(allSessions));
        
        // Recent activity (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        List<PracticeSession> recentSessions = practiceSessionRepository.findRecentSessionsByUser(user, weekAgo);
        stats.put("recentSessions", recentSessions.size());
        
        // Improvement analysis
        stats.put("improvementPercentage", calculateImprovementPercentage(allSessions));
        
        // Section-wise stats
        stats.put("sectionStats", calculateSectionStats(allSessions));
        
        // Vocabulary analysis
        stats.put("vocabularyStats", calculateVocabularyStats(allSessions));
        
        // Recent sessions for history
        stats.put("recentSessionsList", recentSessions.stream()
                .map(this::convertToSessionDto)
                .toList());
        
        return stats;
    }

    private Double calculateAverageScore(List<PracticeSession> sessions) {
        if (sessions.isEmpty()) return 0.0;
        return sessions.stream()
                .mapToDouble(session -> session.getOverallScore() != null ? session.getOverallScore() : 0.0)
                .average()
                .orElse(0.0);
    }

    private Long calculateTotalTimeSpent(List<PracticeSession> sessions) {
        return sessions.stream()
                .mapToLong(session -> session.getDurationSeconds() != null ? session.getDurationSeconds() : 0L)
                .sum();
    }

    private Double calculateImprovementPercentage(List<PracticeSession> sessions) {
        if (sessions.size() < 2) return 0.0;
        
        // Get first and last 5 sessions for comparison
        List<PracticeSession> firstSessions = sessions.subList(Math.max(0, sessions.size() - 5), sessions.size());
        List<PracticeSession> lastSessions = sessions.subList(0, Math.min(5, sessions.size()));
        
        Double firstAverage = calculateAverageScore(firstSessions);
        Double lastAverage = calculateAverageScore(lastSessions);
        
        if (firstAverage == 0.0) return 0.0;
        return ((lastAverage - firstAverage) / firstAverage) * 100;
    }

    private Map<String, Object> calculateSectionStats(List<PracticeSession> sessions) {
        Map<String, Object> sectionStats = new HashMap<>();
        Map<String, List<PracticeSession>> sessionsBySection = sessions.stream()
                .collect(java.util.stream.Collectors.groupingBy(PracticeSession::getTestSection));
        
        for (String section : List.of("part1", "part2", "part3")) {
            List<PracticeSession> sectionSessions = sessionsBySection.getOrDefault(section, List.of());
            Map<String, Object> stats = new HashMap<>();
            stats.put("count", sectionSessions.size());
            stats.put("averageScore", calculateAverageScore(sectionSessions));
            sectionStats.put(section, stats);
        }
        
        return sectionStats;
    }

    private Map<String, Object> calculateVocabularyStats(List<PracticeSession> sessions) {
        Map<String, Object> vocabStats = new HashMap<>();
        
        // Count unique words from transcripts
        long totalWords = sessions.stream()
                .mapToLong(session -> {
                    if (session.getTranscript() != null) {
                        return session.getTranscript().split("\\s+").length;
                    }
                    return 0L;
                })
                .sum();
        
        vocabStats.put("totalWords", totalWords);
        vocabStats.put("averageWordsPerSession", sessions.isEmpty() ? 0.0 : (double) totalWords / sessions.size());
        
        return vocabStats;
    }

    private Map<String, Object> convertToSessionDto(PracticeSession session) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", session.getId());
        dto.put("question", session.getQuestion());
        dto.put("testSection", session.getTestSection());
        dto.put("overallScore", session.getOverallScore());
        dto.put("createdAt", session.getCreatedAt());
        dto.put("durationSeconds", session.getDurationSeconds());
        return dto;
    }
}
