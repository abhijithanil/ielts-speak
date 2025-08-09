package com.ielts.speakingapp.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularySuggestion {
    private String word;
    private String suggestion;
    private String context;
}
