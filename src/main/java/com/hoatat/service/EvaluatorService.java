package com.hoatat.service;

import com.hoatat.dto.EvaluationResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class EvaluatorService {
    private final ChatClient chatClient;
    private final MemoryService memoryService;

    public EvaluatorService(ChatClient.Builder chatClientBuilder, MemoryService memoryService) {
        this.chatClient = chatClientBuilder.build();
        this.memoryService = memoryService;
    }

    public EvaluationResult evaluate(String userGoal, String executionResult) throws Exception {
        String prompt = """
            You are an evaluator.
    
            Evaluate the result.
    
            User Goal:
            %s
    
            Result:
            %s
    
            Return ONLY JSON:
            {
              "pass": true/false,
              "score": 0.0-1.0,
              "feedback": "..."
            }
            """.formatted(userGoal, executionResult);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        ObjectMapper mapper = new ObjectMapper();
        EvaluationResult evaluationResult = mapper.readValue(response, EvaluationResult.class);

        memoryService.add(
                "critic",
                evaluationResult.getFeedback()
        );

        return evaluationResult;
    }
}
