package com.ielts.speakingapp.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentenceCorrection {
    private String original;
    private String corrected;
    private String explanation;
}
