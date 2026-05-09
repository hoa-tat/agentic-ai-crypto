package com.hoatat.service;

import com.hoatat.dto.RouteType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentService {

    private final ToolService toolService;
    private final RouterService routerService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentService(ToolService toolService, RouterService routerService,
                        ChatClient.Builder chatClientBuilder) {
        this.toolService = toolService;
        this.routerService = routerService;
        this.chatClient = chatClientBuilder.build();
    }

    public String handle(String userInput) throws Exception {
        return handleWithLLM(userInput);
    }

    /*public String handle(String userInput) throws Exception {

        RouteType route = routerService.route(userInput);

        switch (route) {
            case DIRECT_API:
                return handleDirectApi(userInput);

            case LLM_AGENT:
                return handleWithLLM(userInput);

            default:
                return "Unknown route";
        }
    }*/

    public String handleWithLLM(String input) throws Exception {
        // 1. Ask LLM to decide action
        String prompt = """
        You are an AI agent.

        Available actions:
        - get_price(symbol)
        - get_ohlc(symbol, interval, linit)
        - compare_price(symbols[])
        - analyze_trend(symbol, interval, limit)
        
        Rules:
        - Return ONLY JSON
        - No explanation
        - symbol must be BTC, ETH, ADA
        - interval must be 1m, 5m, 15m, 1h, 4h, 1d      
        - Use best action for the question
        
        Example:
        
        User: giá BTC
        {
          "action": "get_price",
          "symbol": "BTC"
        }
        
        User: dữ liệu nến ETH 5 phút
        {
          "action": "get_ohlc",
          "symbol": "ETH",
          "interval": "5m",
          "limit": 10
        }
        
        User: so sánh BTC và ETH
        {
          "action": "compare_price",
          "symbols": ["BTC", "ETH"]
        }
        
        User: phân tích xu hướng BTC
        {
          "action": "analyze_trend",
          "symbol": "BTC",
          "interval": "1h",
          "limit": 20
        }
        
        User: %s
        """.formatted(input.toUpperCase());

        String response = chatClient
                .prompt()
                .system("You must return valid JSON only. No explanations.")
                .user(prompt)
                .call()
                .content();

        // parse + execute (giống phase 2 bạn đã làm)
        return executeAction(response);
    }

    public String analyzeTrend(String symbol, String interval, int limit) {

        String ohlcData = toolService.getOHLC(symbol, interval, limit);

        String prompt = """
        You are a trading analyst.
    
        Analyze the trend based on OHLC data.
    
        Return:
        - trend (UP / DOWN / SIDEWAYS)
        - short explanation
    
        Data:
        %s
        """.formatted(ohlcData);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String executeAction(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);

        String action = node.get("action").asText();

        switch (action) {
            case "get_price":
                String symbol = node.get("symbol").asText();
                return toolService.getPrice(symbol);
            case "get_ohlc":
                String sym = node.get("symbol").asText();
                String interval = node.get("interval").asText();
                int limit = node.has("limit") ? node.get("limit").asInt() : 10;
                return toolService.getOHLC(sym, interval, limit);
            case "compare_price":
                List<String> symbols = new ArrayList<>();
                node.get("symbols").forEach(s -> symbols.add(s.asText()));
                return toolService.comparePrice(symbols);
            case "analyze_trend":
                return analyzeTrend(
                        node.get("symbol").asText(),
                        node.get("interval").asText(),
                        node.has("limit") ? node.get("limit").asInt() : 20
                );
            default:
                return "Unknown action";
        }
    }
}
