package com.hoatat.service;

import com.hoatat.dto.Plan;
import com.hoatat.dto.PlanStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class PlanExecutorTool {
    private final ToolExecutor toolExecutor;
    private final ChatClient chatClient;
    private final PlannerServiceTool plannerService;

    public PlanExecutorTool(ToolExecutor toolExecutor ,ChatClient.Builder chatClientBuilder,
                            PlannerServiceTool plannerService) {
        this.toolExecutor = toolExecutor;
        this.chatClient = chatClientBuilder.build();
        this.plannerService = plannerService;
    }

    public Object handle(String userInput) throws Exception {

        // 1. Create plan
        Plan plan = plannerService.createPlan(userInput);

        log.info("Plan: {}", plan);

        // 2. Execute plan
        Object result = executePlan(plan);

        return result;
    }

    public Object executePlan(Plan plan) {
        Map<String,Object> context = new HashMap<>();

        Object lastResult = null;
        for (PlanStep step : plan.getSteps()) {
            if ("final_answer".equals(step.getAction())) {
                return formatFinalAnswer(context, lastResult);
            }

            lastResult = toolExecutor.execute(step.getAction(), step.getParams()
                    , context).toString();

            context.put(step.getAction(), lastResult);
        }
        return lastResult;
    }

    private String formatFinalAnswer(Map<String,Object> context, Object analysisResult) {
        String toolResults =
                context.entrySet()
                        .stream()
                        .map(e ->
                                e.getKey()
                                        + ":\n"
                                        + e.getValue()
                        )
                        .collect(Collectors.joining("\n\n"));

        String prompt = """
                You are a professional crypto analyst.
            
                Convert the analysis into a clean final response for the user.
            
                User Goal:
                %s

                Rules:
                - concise
                - professional
                - easy to understand
                - no markdown
            
                Analysis:
                %s
            """.formatted(toolResults, analysisResult);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
