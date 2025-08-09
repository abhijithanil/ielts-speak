package com.ielts.speakingapp.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String section; // part1, part2, part3
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Column(columnDefinition = "TEXT")
    private String topic; // For Part 2 cue cards and Part 3 question groups
    
    @ElementCollection
    @CollectionTable(name = "question_bullet_points", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "bullet_point", columnDefinition = "TEXT")
    private List<String> bulletPoints; // For Part 2 cue cards
    
    @Column(columnDefinition = "TEXT")
    private String sampleAnswer; // For Part 2 cue cards
    
    @Column(nullable = false)
    private String questionType; // questions, cue_card
    
    @Column(name = "question_group")
    private String questionGroup; // For Part 3 related questions
    
    @Column(name = "question_order")
    private Integer questionOrder; // For Part 3 question ordering within a group
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (usageCount == null) {
            usageCount = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
