package com.hoatat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExecutionRecord {
    private String goal;

    private String plan;

    private String result;

    private boolean success;

    private LocalDateTime createdAt;
}
