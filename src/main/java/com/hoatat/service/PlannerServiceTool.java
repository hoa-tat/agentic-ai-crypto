package com.hoatat.service;


import com.hoatat.dto.Plan;
import com.hoatat.dto.PlanStep;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlannerServiceTool {
    private final ChatClient chatClient;
    public PlannerServiceTool(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Plan createPlan(String userInput) {

        String prompt = """
        You are an AI planner.

        Break the user request into steps.

        Available actions:
        - get_price(symbol)
        - get_ohlc(symbol, interval, limit)
        - analyze_trend
        - compare_price(symbols[])
        - final_answer

        Rules:
        - Return ONLY JSON
        - Keep steps minimal but complete
        - NEVER use placeholder values, always use actual values from the user request or memory
        - Extract real symbols from user input
        - Valid symbols: BTC, ETH, ADA, ZEC

        Example:
                {
                  "steps": [
                    {
                      "step": 1,
                      "action": "get_ohlc",
                      "params":{
                         "symbol":"BTC",
                         "interval":"1h",
                         "limit":50
                       }
                    },
                    {
                      "step": 2,
                      "action": "analyze_trend",
                      "params":{
                         "symbol":"BTC",
                         "interval":"1h",
                         "limit":50
                       }
                    },
                    {
                      "step": 3,
                      "action": "final_answer"
                    }
                  ]
                }

        User: %s
        """.formatted(userInput);
        //""".formatted(memoryService.asText(), userInput);
        Plan plan = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(Plan.class);

        validatePlan(plan);

        return plan;
    }

    private void validatePlan(Plan plan) {

        List<String> validActions = List.of(
                "get_price",
                "get_ohlc",
                "analyze_trend",
                "compare_price",
                "final_answer"
        );

        for (PlanStep step : plan.getSteps()) {

            if (!validActions.contains(step.getAction())) {

                throw new IllegalArgumentException(
                        "Invalid action: " + step.getAction()
                );
            }
        }
    }
}
