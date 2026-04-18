package com.sleep.platform.service;

import com.sleep.platform.domain.request.SleepReplayRequest;
import com.sleep.platform.domain.response.SleepReplayResponse;

public interface SleepReplayService {

    SleepReplayResponse createReplayTask(SleepReplayRequest request);
}
