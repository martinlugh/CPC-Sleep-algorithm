package com.sleep.platform.service;

import com.sleep.platform.domain.request.SleepConfigUpdateRequest;
import com.sleep.platform.domain.response.SleepConfigResponse;

public interface SleepConfigService {

    SleepConfigResponse getCurrentConfig();

    SleepConfigResponse updateConfig(SleepConfigUpdateRequest request);
}
