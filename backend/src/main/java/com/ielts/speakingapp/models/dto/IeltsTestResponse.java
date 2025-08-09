package com.ielts.speakingapp.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IeltsTestResponse {
    
    @JsonProperty("part1")
    private Part1 part1;
    
    @JsonProperty("part2")
    private Part2 part2;
    
    @JsonProperty("part3")
    private Part3 part3;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part1 {
        @JsonProperty("questions")
        private List<String> questions;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part2 {
        @JsonProperty("topic")
        private String topic;
        
        @JsonProperty("bullet_points")
        private List<String> bulletPoints;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part3 {
        @JsonProperty("discussion_questions")
        private List<String> discussionQuestions;
    }
}
