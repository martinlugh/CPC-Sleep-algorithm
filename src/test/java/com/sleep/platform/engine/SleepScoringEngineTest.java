package com.sleep.platform.engine;

import com.sleep.platform.domain.model.DailySleepAggregationResult;
import com.sleep.platform.domain.model.MainSleepSummaryResult;
import com.sleep.platform.domain.model.SleepScoreResult;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SleepScoringEngineTest {

    @Test
    void shouldScoreSleep() {
        SleepScoringEngine engine = new SleepScoringEngine();
        MainSleepSummaryResult summary = new MainSleepSummaryResult();
        summary.setMainSleepDeepMinutes(90);
        summary.setMainSleepLightMinutes(180);
        summary.setMainSleepRemMinutes(80);
        summary.setMainSleepAwakeMinutes(20);
        summary.setMainSleepTotalMinutes(350);
        summary.setMainSleepAwakenCount(2);
        summary.setMainSleepEfficiency(0.9);
        DailySleepAggregationResult daily = new DailySleepAggregationResult();
        daily.setDailyTotalSleepMinutes(390);
        daily.setDaytimeNapTotalMinutes(40);
        daily.setDaytimeNapCount(1);

        SleepSegmentAnalysisResult segment = new SleepSegmentAnalysisResult();
        segment.setQualityPassed(true);
        segment.setQualityRemark("运动对齐置信度=0.900");
        SleepScoreResult result = engine.score(summary, daily, List.of(segment));

        Assertions.assertTrue(result.getMainSleepQualityScore() >= 0);
        Assertions.assertTrue(result.getNightlyRecoveryScore() >= 0);
        Assertions.assertNotNull(result.getScoreExplanation());
    }
}
