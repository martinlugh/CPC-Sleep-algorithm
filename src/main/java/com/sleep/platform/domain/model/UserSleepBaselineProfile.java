package com.sleep.platform.domain.model;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.time.LocalTime;

@Data
public class UserSleepBaselineProfile {

    @DecimalMin(value = "0.0", message = "静息心率必须大于等于0")
    private Double restingHeartRateBpm;
    private LocalTime typicalSleepStartTime;
    private LocalTime typicalWakeTime;
    @DecimalMin(value = "0.0", message = "基线HRV必须大于等于0")
    private Double baselineHrvSdnnMs;
    private Integer baselineSleepLatencyMinutes;
    @DecimalMin(value = "0.0", message = "基线睡眠效率必须大于等于0")
    private Double baselineSleepEfficiency;
}
