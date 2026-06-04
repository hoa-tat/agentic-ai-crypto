package com.hoatat.service;

import com.hoatat.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ToolExecutor {
    private final ToolRegistry registry;

    public Object execute(String toolName, java.util.Map<String, Object> params,
                          Map<String,Object> context) {
        AgentTool tool = registry.getTool(toolName);
        if (tool == null) {
            throw new RuntimeException("Tool not found: " + toolName);
        }
        return tool.execute(params, context);
    }
}
