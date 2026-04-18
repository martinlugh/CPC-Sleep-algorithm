package com.sleep.platform.engine;

import com.sleep.platform.domain.model.DailySleepAggregationResult;
import com.sleep.platform.domain.model.MainSleepSummaryResult;
import com.sleep.platform.domain.response.DaytimeNapResultItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailySleepAggregationEngine {

    public DailySleepAggregationResult aggregate(MainSleepSummaryResult mainSummary,
                                                 List<DaytimeNapResultItem> daytimeNapResultList) {
        DailySleepAggregationResult result = new DailySleepAggregationResult();
        int napTotal = 0;
        if (daytimeNapResultList != null) {
            for (DaytimeNapResultItem item : daytimeNapResultList) {
                napTotal += item.getNapTotalMinutes() == null ? 0 : item.getNapTotalMinutes();
            }
            result.setDaytimeNapCount(daytimeNapResultList.size());
        } else {
            result.setDaytimeNapCount(0);
        }
        result.setDaytimeNapTotalMinutes(napTotal);
        int mainTotal = mainSummary.getMainSleepTotalMinutes() == null ? 0 : mainSummary.getMainSleepTotalMinutes();
        result.setDailyTotalSleepMinutes(mainTotal + napTotal);
        result.setNapResultList(daytimeNapResultList);
        return result;
    }
}
