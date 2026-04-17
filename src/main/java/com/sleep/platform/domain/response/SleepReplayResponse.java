package com.sleep.platform.domain.response;

import com.sleep.platform.domain.enums.ReplayStatus;
import lombok.Data;

@Data
public class SleepReplayResponse {

    private Long replayTaskId;
    private Long sessionId;
    private ReplayStatus replayStatus;
    private String message;
}
