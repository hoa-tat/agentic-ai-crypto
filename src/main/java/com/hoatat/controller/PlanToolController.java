package com.hoatat.controller;

import com.hoatat.service.PlanExecutor;
import com.hoatat.service.PlanExecutorTool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agenticTool")
public class PlanToolController {

    private final PlanExecutorTool planExecutor;

    public PlanToolController(PlanExecutorTool planExecutor) {
        this.planExecutor = planExecutor;
    }

    @GetMapping("/ask")
    public Object ask(@RequestParam String q) throws Exception {
        return planExecutor.handle(q);
    }
}
