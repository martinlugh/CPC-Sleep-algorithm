package com.example.sleepanalysis.service;

import com.example.sleepanalysis.domain.request.SleepAnalysisRequest;
import com.example.sleepanalysis.domain.response.SleepAnalysisResponse;

/**
 * 睡眠分析服务接口。
 */
public interface SleepAnalysisService {

    /**
     * 执行睡眠分析。
     *
     * @param request 睡眠分析请求
     * @return 睡眠分析响应
     */
    SleepAnalysisResponse analyzeSleep(SleepAnalysisRequest request);
}
