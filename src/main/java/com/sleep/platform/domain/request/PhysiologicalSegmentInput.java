package com.sleep.platform.domain.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PhysiologicalSegmentInput {

    @NotNull(message = "生理片段开始时间不能为空")
    private LocalDateTime segmentStartTime;
    @NotEmpty(message = "RRI数组不能为空")
    private List<Double> rriMsList;
    @NotNull(message = "平均心率不能为空")
    private Double averageHeartRateBpm;
}
