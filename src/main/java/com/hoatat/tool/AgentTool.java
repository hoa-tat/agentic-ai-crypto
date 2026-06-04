package com.hoatat.tool;

import java.util.Map;

public interface AgentTool {

    String getName();

    String getDescription();

    Object execute(Map<String, Object> params,
                   Map<String,Object> context);
}
