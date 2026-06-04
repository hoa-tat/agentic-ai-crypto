package com.hoatat.memory;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AgentMemory {

    private String id;

    private String goal;

    private String plan;

    private String result;

    private String feedback;

    public String toText() {

        return """
        Goal:
        %s

        Plan:
        %s

        Result:
        %s

        Feedback:
        %s
        """.formatted(goal, plan, result, feedback);
    }
}
