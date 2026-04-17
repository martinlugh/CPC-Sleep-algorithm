package com.example.sleepanalysis.service;

import com.example.sleepanalysis.domain.request.SleepAnalysisRequest;
import com.example.sleepanalysis.domain.request.SleepSegmentInput;
import com.example.sleepanalysis.domain.request.UserSleepBaselineProfile;
import com.example.sleepanalysis.domain.response.SleepAnalysisResponse;
import com.example.sleepanalysis.enums.SleepStage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SleepAnalysisServiceImpl 单元测试。
 */
public class SleepAnalysisServiceImplTest {

    @Test
    void shouldProduceCompletedResponseWithScores() {
        SleepAnalysisServiceImpl service = new SleepAnalysisServiceImpl();
        SleepAnalysisRequest request = buildRequest();

        SleepAnalysisResponse response = service.analyzeSleep(request);

        Assertions.assertEquals("COMPLETED", response.getAnalysisStatus());
        Assertions.assertNotNull(response.getSleepStageTimeline());
        Assertions.assertEquals(6, response.getSleepStageTimeline().size());
        Assertions.assertNotNull(response.getSleepQualityScore());
        Assertions.assertNotNull(response.getNightlyRecoveryScore());
        Assertions.assertNotNull(response.getNightlyFatigueScore());
        Assertions.assertTrue(response.getSleepQualityScore() >= 0 && response.getSleepQualityScore() <= 100);
        Assertions.assertTrue(response.getNightlyRecoveryScore() >= 0 && response.getNightlyRecoveryScore() <= 100);
        Assertions.assertTrue(response.getNightlyFatigueScore() >= 0 && response.getNightlyFatigueScore() <= 100);
        Assertions.assertNotEquals(SleepStage.UNKNOWN, response.getDominantSleepStage());
    }

    private SleepAnalysisRequest buildRequest() {
        SleepAnalysisRequest request = new SleepAnalysisRequest();
        request.setAnalysisId("analysis-001");
        request.setUserId("user-001");
        request.setSleepSessionStartTime(OffsetDateTime.parse("2026-04-10T22:00:00+08:00"));
        request.setSleepSessionEndTime(OffsetDateTime.parse("2026-04-10T22:03:00+08:00"));

        UserSleepBaselineProfile profile = new UserSleepBaselineProfile();
        profile.setUserId("user-001");
        profile.setBaselineRestingHeartRate(62.0D);
        profile.setBaselineRespirationRate(14.0D);
        profile.setHabitualSleepStartClock("22:30");
        profile.setHabitualWakeUpClock("06:30");
        request.setUserSleepBaselineProfile(profile);

        List<SleepSegmentInput> segments = new ArrayList<>();
        OffsetDateTime start = OffsetDateTime.parse("2026-04-10T22:00:00+08:00");
        for (int i = 0; i < 6; i++) {
            SleepSegmentInput segment = new SleepSegmentInput();
            segment.setSegmentId("seg-" + (i + 1));
            segment.setSegmentStartTime(start.plusSeconds(i * 30L));
            segment.setSegmentEndTime(start.plusSeconds((i + 1L) * 30L));
            segment.setHeartRateSeries(buildSeries(64.0D + i, 60));
            segment.setRespirationRateSeries(buildSeries(14.0D + i * 0.1D, 60));
            segment.setBodyMovementSeries(buildSeries(0.15D + i * 0.02D, 60));
            segment.setSpo2Series(buildSeries(97.0D, 60));
            segments.add(segment);
        }

        request.setSleepSegmentInputs(segments);
        return request;
    }

    private List<Double> buildSeries(double base, int size) {
        List<Double> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            out.add(base + Math.sin(i * 0.2D) * 0.8D);
        }
        return out;
    }
}
