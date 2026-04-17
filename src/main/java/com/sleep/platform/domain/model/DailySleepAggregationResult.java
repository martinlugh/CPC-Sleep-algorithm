package com.sleep.platform.domain.model;

import com.sleep.platform.domain.response.DaytimeNapResultItem;
import lombok.Data;

import java.util.List;

@Data
public class DailySleepAggregationResult {

    private Integer dailyTotalSleepMinutes;
    private Integer daytimeNapTotalMinutes;
    private Integer daytimeNapCount;
    private List<DaytimeNapResultItem> napResultList;
}
