package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.domain.request.SleepSegmentInput;
import com.example.sleepanalysis.enums.SleepStage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * QualityGateEngine 单元测试。
 */
public class QualityGateEngineTest {

    @Test
    void shouldForceUnknownWhenMissingRateAboveTwentyPercent() {
        QualityGateEngine engine = new QualityGateEngine();

        SleepSegmentInput input = new SleepSegmentInput();
        input.setSegmentId("seg-1");
        input.setSegmentStartTime(OffsetDateTime.parse("2026-04-10T22:00:00+08:00"));
        input.setSegmentEndTime(OffsetDateTime.parse("2026-04-10T22:00:30+08:00"));
        input.setHeartRateSeries(Arrays.asList(70.0D, null, 72.0D, null, 71.0D));
        input.setRespirationRateSeries(Arrays.asList(14.0D, 14.0D, null, 15.0D, 14.0D));
        input.setBodyMovementSeries(Arrays.asList(0.1D, 0.2D, 0.1D, null, 0.3D));

        QualityGateEngine.QualityGateResult result = engine.evaluate(input);

        Assertions.assertFalse(result.isPassed());
        Assertions.assertEquals(SleepStage.UNKNOWN, result.getForcedStage());
        Assertions.assertTrue(result.getMissingRate() > 0.20D);
    }
}
