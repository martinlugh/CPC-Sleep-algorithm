package com.sleep.platform.domain.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DaytimeNapResultItem {

    private LocalDateTime napStartTime;
    private LocalDateTime napEndTime;
    private Integer napTotalMinutes;
    private List<SleepStageTimelineItem> napStageTimeline;
}
