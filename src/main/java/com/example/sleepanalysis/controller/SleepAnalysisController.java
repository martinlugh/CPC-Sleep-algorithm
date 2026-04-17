package com.example.sleepanalysis.controller;

import com.example.sleepanalysis.domain.request.SleepAnalysisRequest;
import com.example.sleepanalysis.domain.response.SleepAnalysisResponse;
import com.example.sleepanalysis.service.SleepAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 睡眠分析控制器。
 */
@RestController
@RequestMapping("/api/sleep")
public class SleepAnalysisController {

    /** 睡眠分析服务实现。 */
    private final SleepAnalysisService sleepAnalysisService;

    /**
     * 构造函数。
     *
     * @param sleepAnalysisService 睡眠分析服务
     */
    public SleepAnalysisController(SleepAnalysisService sleepAnalysisService) {
        this.sleepAnalysisService = sleepAnalysisService;
    }

    /**
     * 睡眠分析接口。
     *
     * @param request 睡眠分析请求
     * @return 睡眠分析响应
     */
    @PostMapping("/analyze")
    public ResponseEntity<SleepAnalysisResponse> analyze(@Valid @RequestBody SleepAnalysisRequest request) {
        return ResponseEntity.ok(sleepAnalysisService.analyzeSleep(request));
    }
}
