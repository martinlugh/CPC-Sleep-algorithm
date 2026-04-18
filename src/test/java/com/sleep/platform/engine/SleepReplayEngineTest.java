package com.sleep.platform.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleep.platform.domain.entity.SleepAnalysisResultEntity;
import com.sleep.platform.domain.entity.SleepRawMotionSegmentEntity;
import com.sleep.platform.domain.entity.SleepRawPhysiologicalSegmentEntity;
import com.sleep.platform.domain.entity.SleepRawSessionEntity;
import com.sleep.platform.domain.model.ReplayComparisonResult;
import com.sleep.platform.domain.response.SleepAnalysisResponse;
import com.sleep.platform.mapper.SleepAnalysisResultMapper;
import com.sleep.platform.mapper.SleepRawMotionSegmentMapper;
import com.sleep.platform.mapper.SleepRawPhysiologicalSegmentMapper;
import com.sleep.platform.mapper.SleepRawSessionMapper;
import com.sleep.platform.service.SleepAnalysisService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

class SleepReplayEngineTest {

    @Test
    void shouldReplayAndCompareMetrics() {
        SleepRawSessionMapper sessionMapper = Mockito.mock(SleepRawSessionMapper.class);
        SleepRawPhysiologicalSegmentMapper physiologicalMapper = Mockito.mock(SleepRawPhysiologicalSegmentMapper.class);
        SleepRawMotionSegmentMapper motionMapper = Mockito.mock(SleepRawMotionSegmentMapper.class);
        SleepAnalysisResultMapper resultMapper = Mockito.mock(SleepAnalysisResultMapper.class);
        SleepAnalysisService sleepAnalysisService = Mockito.mock(SleepAnalysisService.class);

        SleepRawSessionEntity session = new SleepRawSessionEntity();
        session.setId(1L);
        session.setUserId("u1");
        session.setAnalysisDate(LocalDate.of(2026,4,1));
        Mockito.when(sessionMapper.selectById(1L)).thenReturn(session);

        SleepRawPhysiologicalSegmentEntity p = new SleepRawPhysiologicalSegmentEntity();
        p.setSessionId(1L);
        p.setSegmentStartTime(LocalDateTime.of(2026,4,1,22,0));
        p.setAverageHeartRateBpm(BigDecimal.valueOf(60));
        p.setRriJson("[800,810,790,805]");
        Mockito.when(physiologicalMapper.selectList(Mockito.any())).thenReturn(List.of(p));

        SleepRawMotionSegmentEntity m = new SleepRawMotionSegmentEntity();
        m.setSessionId(1L);
        m.setMotionSegmentStartTime(LocalDateTime.of(2026,4,1,22,0));
        m.setStepsInEightMinutes(20);
        Mockito.when(motionMapper.selectList(Mockito.any())).thenReturn(List.of(m));

        SleepAnalysisResultEntity oldResult = new SleepAnalysisResultEntity();
        oldResult.setMainSleepTotalMinutes(360);
        oldResult.setDaytimeNapTotalMinutes(20);
        oldResult.setDailyTotalSleepMinutes(380);
        oldResult.setMainSleepQualityScore(BigDecimal.valueOf(82));
        Mockito.when(resultMapper.selectOne(Mockito.any())).thenReturn(oldResult);

        SleepAnalysisResponse replayResponse = new SleepAnalysisResponse();
        replayResponse.setSessionId(2L);
        replayResponse.setMainSleepTotalMinutes(350);
        replayResponse.setDaytimeNapTotalMinutes(30);
        replayResponse.setDailyTotalSleepMinutes(380);
        replayResponse.setMainSleepQualityScore(80.0);
        Mockito.when(sleepAnalysisService.analyzeSleep(Mockito.any())).thenReturn(replayResponse);

        SleepReplayEngine engine = new SleepReplayEngine(sessionMapper, physiologicalMapper, motionMapper, resultMapper,
                sleepAnalysisService, new ObjectMapper());
        ReplayComparisonResult result = engine.replayAndCompare(1L);

        Assertions.assertEquals(1L, result.getOriginalSessionId());
        Assertions.assertEquals(2L, result.getReplaySessionId());
        Assertions.assertTrue(result.getCompareMetrics().containsKey("mainSleepTotalMinutes"));
    }
}
