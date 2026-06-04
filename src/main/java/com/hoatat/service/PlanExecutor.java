package com.hoatat.service;

import com.hoatat.dto.EvaluationResult;
import com.hoatat.memory.AgentMemory;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class PlanExecutor {

    private final ToolService toolService;
    private final ChatClient chatClient;
    private final PlannerService plannerService;
    private final EvaluatorService evaluatorService;
    private final MemoryService memoryService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PlanExecutor(ToolService toolService, ChatClient.Builder chatClientBuilder,
                        PlannerService plannerService, EvaluatorService evaluatorService, MemoryService memoryService) {
        this.toolService = toolService;
        this.chatClient = chatClientBuilder.build();
        this.plannerService = plannerService;
        this.evaluatorService = evaluatorService;
        this.memoryService = memoryService;
    }

    public String handle(String userInput) throws Exception {

        // 1. Create plan
        String plan = plannerService.createPlan(userInput);

        log.info("Plan: {}", plan);

        // 2. Execute plan
        String result = executePlan(plan);

        return result;
    }

    public String executePlan(String planJson) throws Exception {

        JsonNode root = mapper.readTree(planJson);
        JsonNode steps = root.get("steps");

        String lastResult = null;

        for (JsonNode step : steps) {

            String action = step.get("action").asText();

            switch (action) {
                case "get_ohlc":
                    lastResult = toolService.getOHLC(
                            step.get("symbol").asText(),
                            step.get("interval").asText(),
                            step.get("limit").asInt()
                    );
                    break;

                case "analyze_trend":
                    lastResult = analyzeTrend(lastResult);
                    break;

                case "compare_price":
                    List<String> symbols = new ArrayList<>();
                    step.get("symbols").forEach(s -> symbols.add(s.asText()));
                    lastResult = toolService.comparePrice(symbols);
                    break;

                case "final_answer":
                    return formatFinalAnswer(lastResult);
            }
        }

        return lastResult;
    }

    private String analyzeTrend(String ohlcData) {

        String prompt = """
            Analyze trend from this data.
        
            Return:
            - trend
            - explanation
        
            Data:
            %s
            """.formatted(ohlcData);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String formatFinalAnswer(String analysisResult) {

        String prompt = """
                You are a professional crypto analyst.
            
                Convert the analysis into a clean final response for the user.
            
                Rules:
                - concise
                - professional
                - easy to understand
                - no markdown
            
                Analysis:
                %s
            """.formatted(analysisResult);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    //Re-plan Engine
    public String runAgentReplan(String userGoal) throws Exception {

        while(true) {

            // 1. Create plan
            String plan = plannerService.createPlan(userGoal);
            log.info("Plan: {}", plan);

            // 2. Execute
            String result = executePlan(plan);
            log.info("Execution result: {}", result);
            memoryService.add("assistant", userGoal);

            // 3. Evaluate
            EvaluationResult eval =
                    evaluatorService.evaluate(userGoal, result);
            log.info("Evaluation score: {}", eval);

            // 4. PASS
            if (eval.isPass()) {
                return result;
            }

            AgentMemory memory = new AgentMemory();
            memory.setGoal(userGoal);
            memory.setPlan(plan.toString());
            memory.setResult(result);
            memory.setFeedback(eval.getFeedback());
            memoryService.saveMemory(memory);

            // 5. FAIL → improve prompt
            userGoal = """
                The previous plan failed.
                
                Feedback:
                %s
                
                Create a BETTER plan.
                """.formatted(eval.getFeedback());
        }
    }

    //Retry Engine
    public String runAgentRetry(String userGoal) throws Exception {

        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {

            // 1. Create plan
            String plan = plannerService.createPlan(userGoal);
            log.info("Plan: {}", plan);

            // 2. Execute
            String result = executePlan(plan);
            log.info("Execution result: {}", result);

            // 3. Evaluate
            EvaluationResult eval =
                    evaluatorService.evaluate(userGoal, result);
            log.info("Evaluation score: {}", eval);

            // 4. PASS
            if (eval.isPass()) {
                return result;
            }

            // 5. FAIL → improve prompt
            userGoal = """
                Previous attempt failed.
        
                Feedback:
                %s
        
                Original goal:
                %s
                """.formatted(eval.getFeedback(), userGoal);
        }

        return "Agent failed after retries";
    }
}
