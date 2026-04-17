package com.sleep.platform.service;

import com.sleep.platform.domain.request.SleepAnalysisRequest;
import com.sleep.platform.domain.response.SleepAnalysisResponse;

public interface SleepAnalysisService {

    SleepAnalysisResponse analyzeSleep(SleepAnalysisRequest request);
}
