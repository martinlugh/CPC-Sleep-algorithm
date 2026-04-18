package com.sleep.platform.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SleepReplayRequest {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
    @NotNull(message = "是否强制重跑不能为空")
    private Boolean forceReplay;
}
