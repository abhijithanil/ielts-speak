package com.ielts.speakingapp.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ielts.speakingapp.models.dto.SpeechAnalysisResponse;
import com.ielts.speakingapp.models.dto.SentenceCorrection;
import com.ielts.speakingapp.models.dto.VocabularySuggestion;
import com.ielts.speakingapp.models.dto.PronunciationTip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeepSeekAnalysisService {

    @Autowired
    private DeepSeekService deepSeekService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpeechAnalysisResponse analyzeSpeech(String question, String transcript) {
        try {
            String analysisJson = deepSeekService.analyzeSpeech(question, transcript);
            return parseAnalysisResponse(analysisJson, transcript, question);
        } catch (Exception e) {
            System.err.println("Error analyzing speech: " + e.getMessage());
            return createMockResponse(question, transcript);
        }
    }

    private SpeechAnalysisResponse parseAnalysisResponse(String jsonResponse, String transcript, String question) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            SpeechAnalysisResponse response = new SpeechAnalysisResponse();
            response.setTranscript(root.path("transcript").asText(transcript));
            response.setFeedback(root.path("feedback").asText("Good response"));
            response.setOverallScore(root.path("overallScore").asDouble(7.0));
            response.setFluencyScore(root.path("fluencyScore").asDouble(7.0));
            response.setLexicalScore(root.path("lexicalScore").asDouble(7.0));
            response.setGrammaticalScore(root.path("grammaticalScore").asDouble(7.0));
            response.setPronunciationScore(root.path("pronunciationScore").asDouble(7.0));
            
            // Parse sentence corrections
            List<SentenceCorrection> corrections = new ArrayList<>();
            JsonNode correctionsNode = root.path("sentenceCorrections");
            if (correctionsNode.isArray()) {
                for (JsonNode correction : correctionsNode) {
                    SentenceCorrection sc = new SentenceCorrection();
                    sc.setOriginal(correction.path("original").asText());
                    sc.setCorrected(correction.path("corrected").asText());
                    sc.setExplanation(correction.path("explanation").asText());
                    corrections.add(sc);
                }
            }
            response.setSentenceCorrections(corrections);
            
            // Parse vocabulary suggestions
            List<VocabularySuggestion> vocabSuggestions = new ArrayList<>();
            JsonNode vocabNode = root.path("vocabularySuggestions");
            if (vocabNode.isArray()) {
                for (JsonNode vocab : vocabNode) {
                    VocabularySuggestion vs = new VocabularySuggestion();
                    vs.setWord(vocab.path("word").asText());
                    vs.setSuggestion(vocab.path("suggestion").asText());
                    vs.setContext(vocab.path("context").asText());
                    vocabSuggestions.add(vs);
                }
            }
            response.setVocabularySuggestions(vocabSuggestions);
            
            // Parse pronunciation tips
            List<PronunciationTip> pronunciationTips = new ArrayList<>();
            JsonNode pronNode = root.path("pronunciationTips");
            if (pronNode.isArray()) {
                for (JsonNode pron : pronNode) {
                    PronunciationTip pt = new PronunciationTip();
                    pt.setWord(pron.path("word").asText());
                    pt.setTip(pron.path("tip").asText());
                    pronunciationTips.add(pt);
                }
            }
            response.setPronunciationTips(pronunciationTips);
            
            return response;
        } catch (Exception e) {
            System.err.println("Error parsing analysis response: " + e.getMessage());
            return createMockResponse(question, transcript);
        }
    }

    private SpeechAnalysisResponse createMockResponse(String question, String transcript) {
        SpeechAnalysisResponse response = new SpeechAnalysisResponse();
        response.setTranscript(transcript);
        response.setFeedback("Good response with clear ideas. Work on fluency and pronunciation.");
        response.setOverallScore(7.0);
        response.setFluencyScore(6.5);
        response.setLexicalScore(7.5);
        response.setGrammaticalScore(7.0);
        response.setPronunciationScore(6.5);
        response.setSentenceCorrections(new ArrayList<>());
        response.setVocabularySuggestions(new ArrayList<>());
        response.setPronunciationTips(new ArrayList<>());
        return response;
    }
}
