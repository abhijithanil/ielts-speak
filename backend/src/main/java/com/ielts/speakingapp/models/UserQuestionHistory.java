package com.ielts.speakingapp.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
    
    @Column(name = "practice_session_id")
    private String practiceSessionId;
    
    @Column(name = "test_section")
    private String testSection; // part1, part2, part3
    
    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }
}
