package com.hoatat.service;

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
    private final ObjectMapper mapper = new ObjectMapper();

    public PlanExecutor(ToolService toolService, ChatClient.Builder chatClientBuilder,
                        PlannerService plannerService) {
        this.toolService = toolService;
        this.chatClient = chatClientBuilder.build();
        this.plannerService = plannerService;
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
}
