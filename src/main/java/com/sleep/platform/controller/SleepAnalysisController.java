package com.sleep.platform.controller;

import com.sleep.platform.common.ApiResponse;
import com.sleep.platform.domain.request.SleepAnalysisRequest;
import com.sleep.platform.domain.response.SleepAnalysisResponse;
import com.sleep.platform.service.SleepAnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sleep")
public class SleepAnalysisController {

    private final SleepAnalysisService sleepAnalysisService;

    public SleepAnalysisController(SleepAnalysisService sleepAnalysisService) {
        this.sleepAnalysisService = sleepAnalysisService;
    }

    @PostMapping("/analyze")
    public ApiResponse<SleepAnalysisResponse> analyze(@Valid @RequestBody SleepAnalysisRequest request) {
        return ApiResponse.success(sleepAnalysisService.analyzeSleep(request));
    }
}
