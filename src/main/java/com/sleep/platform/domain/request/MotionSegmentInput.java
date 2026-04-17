package com.sleep.platform.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MotionSegmentInput {

    @NotNull(message = "运动片段开始时间不能为空")
    private LocalDateTime motionSegmentStartTime;
    @NotNull(message = "8分钟步数不能为空")
    private Integer stepsInEightMinutes;
}
