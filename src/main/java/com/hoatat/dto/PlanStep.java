package com.hoatat.dto;

import lombok.Data;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Data
public class PlanStep {
    private int step;

    private String action;

    private String symbol;

    private Map<String,Object> params;

    @Override
    public String toString() {
        return "PlanStep{" +
                "step=" + step +
                ", action='" + action + '\'' +
                ", symbol='" + symbol + '\'' +
                ", params=" + params +
                '}';
    }
}
