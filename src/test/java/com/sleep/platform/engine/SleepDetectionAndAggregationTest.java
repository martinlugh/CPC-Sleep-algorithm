package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
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
import java.util.ArrayList;
import java.util.List;

class SleepDetectionAndAggregationTest {

    @Test
    void shouldDetectSleepOnsetAndWakeUp() {
        SleepAnalysisProperties properties = properties();
        SleepOnsetDetectionEngine onsetEngine = new SleepOnsetDetectionEngine(properties);
        WakeUpDetectionEngine wakeUpEngine = new WakeUpDetectionEngine(properties);
        List<SleepSegmentAnalysisResult> segments = buildNightSegments();

        SleepOnsetDetectionResult onset = onsetEngine.detect(segments, null, segments.get(0).getSegmentStartTime());
        WakeUpDetectionResult wake = wakeUpEngine.detect(segments, 1);

        Assertions.assertNotNull(onset.getMainSleepStartTime());
        Assertions.assertNotNull(wake.getMainSleepWakeUpTime());
    }

    @Test
    void shouldDetectDaytimeNapsAndAggregateMultiNaps() {
        SleepAnalysisProperties properties = properties();
        DaytimeNapDetectionEngine napEngine = new DaytimeNapDetectionEngine(properties);
        DailySleepAggregationEngine dailyEngine = new DailySleepAggregationEngine();

        List<SleepSegmentAnalysisResult> segments = buildDayNaps();
        List<DaytimeNapResultItem> naps = napEngine.detect(segments);
        MainSleepSummaryResult summary = new MainSleepSummaryResult();
        summary.setMainSleepTotalMinutes(360);
        DailySleepAggregationResult daily = dailyEngine.aggregate(summary, naps);

        Assertions.assertTrue(naps.size() >= 2);
        Assertions.assertTrue(daily.getDaytimeNapTotalMinutes() > 0);
        Assertions.assertEquals(summary.getMainSleepTotalMinutes() + daily.getDaytimeNapTotalMinutes(), daily.getDailyTotalSleepMinutes());
    }

    @Test
    void shouldSummarizeMainSleepDuration() {
        SleepSummaryEngine summaryEngine = new SleepSummaryEngine();
        List<SleepSegmentAnalysisResult> segments = buildNightSegments();

        SleepOnsetDetectionResult onset = new SleepOnsetDetectionResult();
        onset.setMainSleepStartTime(segments.get(1).getSegmentStartTime());
        onset.setMainSleepLatencyMinutes(5);
        WakeUpDetectionResult wake = new WakeUpDetectionResult();
        wake.setMainSleepWakeUpTime(segments.get(segments.size() - 1).getSegmentStartTime());

        MainSleepSummaryResult summary = summaryEngine.summarize(segments, onset, wake);
        Assertions.assertTrue(summary.getMainSleepTotalMinutes() > 0);
        Assertions.assertTrue(summary.getMainSleepEfficiency() >= 0.0);
    }

    private SleepAnalysisProperties properties() {
        SleepAnalysisProperties p = new SleepAnalysisProperties();
        p.setSleepOnsetRequiredSegments(2);
        p.setWakeUpRequiredSegments(2);
        p.setDaytimeNapRequiredSegments(2);
        p.setDaytimeNapMinMinutes(20);
        p.setDaytimeNapMergeGapMinutes(15);
        return p;
    }

    private List<SleepSegmentAnalysisResult> buildNightSegments() {
        List<SleepSegmentAnalysisResult> list = new ArrayList<>();
        LocalDateTime t = LocalDateTime.of(2026, 4, 1, 22, 0);
        SleepStage[] stages = {SleepStage.WAKE, SleepStage.LIGHT, SleepStage.DEEP, SleepStage.REM, SleepStage.LIGHT, SleepStage.WAKE, SleepStage.WAKE};
        for (SleepStage stage : stages) {
            SleepSegmentAnalysisResult r = new SleepSegmentAnalysisResult();
            r.setSegmentStartTime(t);
            r.setSmoothedStage(stage);
            r.setQualityPassed(true);
            r.setConfidenceScore(0.8);
            list.add(r);
            t = t.plusMinutes(5);
        }
        return list;
    }

    private List<SleepSegmentAnalysisResult> buildDayNaps() {
        List<SleepSegmentAnalysisResult> list = new ArrayList<>();
        LocalDateTime t = LocalDateTime.of(2026, 4, 2, 12, 0);
        SleepStage[] stages = {
                SleepStage.LIGHT, SleepStage.LIGHT, SleepStage.REM, SleepStage.LIGHT,
                SleepStage.WAKE, SleepStage.WAKE,
                SleepStage.LIGHT, SleepStage.DEEP, SleepStage.LIGHT, SleepStage.LIGHT
        };
        for (SleepStage stage : stages) {
            SleepSegmentAnalysisResult r = new SleepSegmentAnalysisResult();
            r.setSegmentStartTime(t);
            r.setSmoothedStage(stage);
            r.setQualityPassed(true);
            r.setConfidenceScore(0.75);
            list.add(r);
            t = t.plusMinutes(5);
        }
        return list;
    }
}
