package com.hoatat.controller;

import com.hoatat.service.AgentService;
import com.hoatat.service.PlanExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agentic")
public class PlanController {

    private final PlanExecutor planExecutor;

    public PlanController(PlanExecutor planExecutor) {
        this.planExecutor = planExecutor;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String q) throws Exception {
        return planExecutor.handle(q);
    }
}
