package com.ielts.speakingapp.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "practice_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String question;
    
    @Column(name = "test_section")
    private String testSection; // "part1", "part2", "part3"
    
    @Column(columnDefinition = "TEXT")
    private String transcript;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    private Double overallScore;
    private Double fluencyScore;
    private Double lexicalScore;
    private Double grammaticalScore;
    private Double pronunciationScore;
    
    @Column(name = "audio_url")
    private String audioUrl;
    
    @Column(name = "feedback_audio_url")
    private String feedbackAudioUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
