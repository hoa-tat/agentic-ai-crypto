package com.hoatat.tool;

import com.hoatat.service.ToolService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetPriceTool  implements AgentTool {

    private final ToolService toolService;

    public GetPriceTool(ToolService toolService) {
        this.toolService = toolService;
    }

    @Override
    public String getName() {
        return "get_price";
    }

    @Override
    public String getDescription() {
        return "Get latest crypto price. Input is the symbol, e.g. BTC, ETH.";
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String,Object> context) {
        String input = (String) params.get("symbol");
        // call tool service to get price
        return toolService.getPrice(input);
    }
}
