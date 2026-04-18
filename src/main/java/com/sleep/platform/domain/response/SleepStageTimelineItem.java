package com.sleep.platform.domain.response;

import com.sleep.platform.domain.enums.SleepStage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SleepStageTimelineItem {

    private LocalDateTime segmentStartTime;
    private SleepStage stage;
    private Double confidenceScore;
    private Double alignedStepsInFiveMinutes;
    private Double averageHeartRateBpm;
    private List<String> explainTags;
}
