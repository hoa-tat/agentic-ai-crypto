package com.hoatat.service;

import com.hoatat.dto.RouteType;
import org.springframework.stereotype.Service;

@Service
public class RouterService {

    public RouteType route(String input) {
        // Simple routing logic based on keywords
        if (input.toLowerCase().contains(".*\\b(btc|eth|ada)\\b")
                && input.contains("giá")) {
            return RouteType.DIRECT_API;

        }
        return RouteType.LLM_AGENT;
    }
}
