package com.hoatat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class PlannerService {
    private final ChatClient chatClient;

    public PlannerService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String createPlan(String userInput) {

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

        Example:
                {
                  "steps": [
                    {
                      "step": 1,
                      "action": "get_ohlc",
                      "symbol": "BTC",
                      "interval": "1h",
                      "limit": 20
                    },
                    {
                      "step": 2,
                      "action": "analyze_trend",
                      "symbol": "BTC",
                      "interval": "1h",
                      "limit": 20
                    },
                    {
                      "step": 3,
                      "action": "final_answer"
                    }
                  ]
                }

        User: %s
        """.formatted(userInput);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
