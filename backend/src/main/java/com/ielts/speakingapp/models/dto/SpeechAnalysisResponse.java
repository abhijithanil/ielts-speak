package com.ielts.speakingapp.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeechAnalysisResponse {
    private String transcript;
    private String feedback;
    private Double overallScore;
    private Double fluencyScore;
    private Double lexicalScore;
    private Double grammaticalScore;
    private Double pronunciationScore;
    private String feedbackAudioUrl;
    private List<SentenceCorrection> sentenceCorrections;
    private List<VocabularySuggestion> vocabularySuggestions;
    private List<PronunciationTip> pronunciationTips;
}
