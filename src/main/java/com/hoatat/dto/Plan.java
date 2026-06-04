package com.hoatat.dto;

import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Data
@Component
public class Plan {
    private List<PlanStep> steps;

    private final ObjectMapper mapper = new ObjectMapper();

    public Plan parsePlan(String json) throws Exception {

        return mapper.readValue(json, Plan.class);
    }

    @Override
    public String toString() {
        return "Plan{" +
                "steps=" + steps +
                '}';
    }
}
