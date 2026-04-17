package com.sleep.platform.engine;

import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.DailySleepAggregationResult;
import com.sleep.platform.domain.model.MainSleepSummaryResult;
import com.sleep.platform.domain.model.SleepOnsetDetectionResult;
import com.sleep.platform.domain.model.WakeUpDetectionResult;
import com.sleep.platform.domain.response.DaytimeNapResultItem;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class SleepStatisticsResultTest {

    @Test
    void shouldProduceExpectedMainSleepStatistics() {
        SleepSummaryEngine engine = new SleepSummaryEngine();
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 22, 0);
        List<SleepSegmentAnalysisResult> segments = List.of(
                segment(start, SleepStage.WAKE),
                segment(start.plusMinutes(5), SleepStage.LIGHT),
                segment(start.plusMinutes(10), SleepStage.DEEP),
                segment(start.plusMinutes(15), SleepStage.REM),
                segment(start.plusMinutes(20), SleepStage.WAKE),
                segment(start.plusMinutes(25), SleepStage.WAKE),
                segment(start.plusMinutes(30), SleepStage.LIGHT)
        );

        SleepOnsetDetectionResult onset = new SleepOnsetDetectionResult();
        onset.setMainSleepStartTime(start);
        onset.setMainSleepLatencyMinutes(10);
        WakeUpDetectionResult wake = new WakeUpDetectionResult();
        wake.setMainSleepWakeUpTime(start.plusMinutes(35));

        MainSleepSummaryResult summary = engine.summarize(segments, onset, wake);

        Assertions.assertEquals(5, summary.getMainSleepDeepMinutes());
        Assertions.assertEquals(10, summary.getMainSleepLightMinutes());
        Assertions.assertEquals(5, summary.getMainSleepRemMinutes());
        Assertions.assertEquals(15, summary.getMainSleepAwakeMinutes());
        Assertions.assertEquals(20, summary.getMainSleepTotalMinutes());
        Assertions.assertEquals(1, summary.getMainSleepAwakenCount());
        Assertions.assertEquals(20.0 / 35.0, summary.getMainSleepEfficiency(), 1e-9);
    }

    @Test
    void shouldAggregateDailyTotalWhenMainSummaryIsMissing() {
        DailySleepAggregationEngine engine = new DailySleepAggregationEngine();
        DaytimeNapResultItem nap1 = new DaytimeNapResultItem();
        nap1.setNapTotalMinutes(20);
        DaytimeNapResultItem nap2 = new DaytimeNapResultItem();
        nap2.setNapTotalMinutes(15);

        DailySleepAggregationResult result = engine.aggregate(null, List.of(nap1, nap2));

        Assertions.assertEquals(2, result.getDaytimeNapCount());
        Assertions.assertEquals(35, result.getDaytimeNapTotalMinutes());
        Assertions.assertEquals(35, result.getDailyTotalSleepMinutes());
    }

    private SleepSegmentAnalysisResult segment(LocalDateTime time, SleepStage stage) {
        SleepSegmentAnalysisResult result = new SleepSegmentAnalysisResult();
        result.setSegmentStartTime(time);
        result.setSmoothedStage(stage);
        return result;
    }
}
