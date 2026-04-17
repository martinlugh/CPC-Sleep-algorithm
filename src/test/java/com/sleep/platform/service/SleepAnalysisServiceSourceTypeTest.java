package com.sleep.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.entity.SleepRawSessionEntity;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.request.MotionSegmentInput;
import com.sleep.platform.domain.request.PhysiologicalSegmentInput;
import com.sleep.platform.domain.request.SleepAnalysisRequest;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import com.sleep.platform.engine.DailySleepAggregationEngine;
import com.sleep.platform.engine.DaytimeNapDetectionEngine;
import com.sleep.platform.engine.SleepOnsetDetectionEngine;
import com.sleep.platform.engine.SleepScoringEngine;
import com.sleep.platform.engine.SleepSegmentPipelineEngine;
import com.sleep.platform.engine.SleepStateSmoothingEngine;
import com.sleep.platform.engine.SleepSummaryEngine;
import com.sleep.platform.engine.WakeUpDetectionEngine;
import com.sleep.platform.mapper.DaytimeNapResultMapper;
import com.sleep.platform.mapper.SleepAnalysisResultMapper;
import com.sleep.platform.mapper.SleepRawMotionSegmentMapper;
import com.sleep.platform.mapper.SleepRawPhysiologicalSegmentMapper;
import com.sleep.platform.mapper.SleepRawSessionMapper;
import com.sleep.platform.service.impl.SleepAnalysisServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

class SleepAnalysisServiceSourceTypeTest {

    @Test
    void shouldUseNonWatchSourceWhenWatchExistsOnSameDate() {
        SleepAnalysisProperties properties = new SleepAnalysisProperties();
        properties.setCurrentModelVersion("V1");
        properties.setCurrentRuleVersion("R1");

        SleepRawSessionMapper sessionMapper = Mockito.mock(SleepRawSessionMapper.class);
        SleepRawPhysiologicalSegmentMapper physiologicalMapper = Mockito.mock(SleepRawPhysiologicalSegmentMapper.class);
        SleepRawMotionSegmentMapper motionMapper = Mockito.mock(SleepRawMotionSegmentMapper.class);
        SleepAnalysisResultMapper analysisResultMapper = Mockito.mock(SleepAnalysisResultMapper.class);
        DaytimeNapResultMapper napResultMapper = Mockito.mock(DaytimeNapResultMapper.class);

        Mockito.when(sessionMapper.selectCount(Mockito.any())).thenReturn(1L, 0L);

        SleepSegmentPipelineEngine pipelineEngine = Mockito.mock(SleepSegmentPipelineEngine.class);
        SleepSegmentAnalysisResult one = new SleepSegmentAnalysisResult();
        one.setSegmentStartTime(LocalDateTime.of(2026, 4, 1, 22, 0));
        one.setStageAfterCalibration(SleepStage.LIGHT);
        one.setSmoothedStage(SleepStage.LIGHT);
        one.setQualityPassed(true);
        one.setConfidenceScore(0.8);
        one.setRawRriCount(120);
        one.setCleanedRriCount(118);
        one.setHfcPower(1.0);
        one.setLfcPower(0.8);
        one.setVlfcPower(0.2);
        one.setHfcLfcRatio(1.25);
        one.setSd1Ms(12.0);
        one.setSd2Ms(24.0);
        one.setSd2Sd1Ratio(2.0);
        one.setSampleEntropy(1.3);
        one.setStageBeforeCalibration(SleepStage.LIGHT);
        Mockito.when(pipelineEngine.analyzeSingleSegment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(one);

        SleepStateSmoothingEngine smoothingEngine = Mockito.mock(SleepStateSmoothingEngine.class);
        Mockito.when(smoothingEngine.smooth(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
        SleepOnsetDetectionEngine onsetEngine = Mockito.mock(SleepOnsetDetectionEngine.class);
        WakeUpDetectionEngine wakeUpEngine = Mockito.mock(WakeUpDetectionEngine.class);
        DaytimeNapDetectionEngine napEngine = Mockito.mock(DaytimeNapDetectionEngine.class);
        SleepSummaryEngine summaryEngine = Mockito.mock(SleepSummaryEngine.class);
        DailySleepAggregationEngine aggregationEngine = Mockito.mock(DailySleepAggregationEngine.class);
        SleepScoringEngine scoringEngine = Mockito.mock(SleepScoringEngine.class);

        // 该测试重点验证 sourceType 选择，后续步骤使用最小化 mock
        Mockito.when(onsetEngine.detect(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new com.sleep.platform.domain.model.SleepOnsetDetectionResult());
        Mockito.when(wakeUpEngine.detect(Mockito.any(), Mockito.anyInt())).thenReturn(new com.sleep.platform.domain.model.WakeUpDetectionResult());
        Mockito.when(napEngine.detect(Mockito.any())).thenReturn(List.of());
        com.sleep.platform.domain.model.MainSleepSummaryResult summary = new com.sleep.platform.domain.model.MainSleepSummaryResult();
        summary.setMainSleepTotalMinutes(0);
        summary.setMainSleepEfficiency(0.0);
        summary.setMainSleepDeepMinutes(0);
        summary.setMainSleepLightMinutes(0);
        summary.setMainSleepRemMinutes(0);
        summary.setMainSleepAwakeMinutes(0);
        summary.setMainSleepAwakenCount(0);
        Mockito.when(summaryEngine.summarize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(summary);
        com.sleep.platform.domain.model.DailySleepAggregationResult aggregation = new com.sleep.platform.domain.model.DailySleepAggregationResult();
        aggregation.setDailyTotalSleepMinutes(0);
        aggregation.setDaytimeNapCount(0);
        aggregation.setDaytimeNapTotalMinutes(0);
        aggregation.setNapResultList(List.of());
        Mockito.when(aggregationEngine.aggregate(Mockito.any(), Mockito.any())).thenReturn(aggregation);
        com.sleep.platform.domain.model.SleepScoreResult score = new com.sleep.platform.domain.model.SleepScoreResult();
        score.setMainSleepQualityScore(0.0);
        score.setNightlyRecoveryScore(0.0);
        score.setNightlyFatigueScore(0.0);
        score.setDataQualityScore(0.0);
        score.setScoreExplanation(java.util.Map.of());
        Mockito.when(scoringEngine.score(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(score);

        SleepAnalysisServiceImpl service = new SleepAnalysisServiceImpl(properties, sessionMapper, physiologicalMapper, motionMapper,
                analysisResultMapper, napResultMapper, pipelineEngine, smoothingEngine, onsetEngine, wakeUpEngine,
                napEngine, summaryEngine, aggregationEngine, scoringEngine, new ObjectMapper());

        SleepAnalysisRequest request = new SleepAnalysisRequest();
        request.setUserId("user1");
        request.setAnalysisDate(LocalDate.of(2026, 4, 1));
        PhysiologicalSegmentInput p = new PhysiologicalSegmentInput();
        p.setSegmentStartTime(LocalDateTime.of(2026, 4, 1, 22, 0));
        p.setAverageHeartRateBpm(60.0);
        p.setRriMsList(List.of(800.0, 810.0, 790.0, 805.0));
        request.setPhysiologicalSegmentList(List.of(p));
        MotionSegmentInput m = new MotionSegmentInput();
        m.setMotionSegmentStartTime(LocalDateTime.of(2026, 4, 1, 22, 0));
        m.setStepsInEightMinutes(12);
        request.setMotionSegmentList(List.of(m));
        request.setBaselineProfile(new com.sleep.platform.domain.model.UserSleepBaselineProfile());

        service.analyzeSleep(request);

        ArgumentCaptor<SleepRawSessionEntity> captor = ArgumentCaptor.forClass(SleepRawSessionEntity.class);
        Mockito.verify(sessionMapper).insert(captor.capture());
        Assertions.assertEquals("MANUAL_IMPORT", captor.getValue().getSourceType());
    }
}
