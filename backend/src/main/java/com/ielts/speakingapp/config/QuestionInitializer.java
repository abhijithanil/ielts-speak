package com.ielts.speakingapp.config;

import com.ielts.speakingapp.services.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionInitializer implements CommandLineRunner {

    private final QuestionService questionService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default questions...");
        questionService.initializeDefaultQuestions();
        log.info("Default questions initialization completed.");
    }
}
