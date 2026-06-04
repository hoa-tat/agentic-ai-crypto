package com.hoatat.dto;

import lombok.Data;

@Data
public class EvaluationResult {
    private boolean pass;
    private double score;
    private String feedback;
}
