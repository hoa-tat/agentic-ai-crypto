package com.hoatat.service;

import com.hoatat.tool.AgentTool;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolRegistry {
    private final Map<String, AgentTool> tools = new HashMap<>();

    public ToolRegistry(List<AgentTool> toolList) {
        for (AgentTool tool : toolList) {
            tools.put(tool.getName(), tool
            );
        }
    }

    public AgentTool getTool(String name) {
        return tools.get(name);
    }

    public Collection<AgentTool> getAllTools() {
        return tools.values();
    }
}
