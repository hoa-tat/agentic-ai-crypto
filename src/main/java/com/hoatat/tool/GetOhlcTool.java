package com.hoatat.tool;

import com.hoatat.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetOhlcTool implements AgentTool {

    private final ToolService toolService;

    @Override
    public String getName() {
        return "get_ohlc";
    }

    @Override
    public String getDescription() {
        return "Get OHLC data for a symbol. Input is the symbol, e.g. BTC, ETH.";
    }

    @Override
    public Object execute(java.util.Map<String, Object> params, Map<String,Object> context) {
        String result =
                toolService.getOHLC(
                        (String) params.get("symbol"),
                        (String) params.get("interval"),
                        ((Number) params.get("limit")).intValue()
                );

        //context.put("get_ohlc", result);

        return result;
    }
}
