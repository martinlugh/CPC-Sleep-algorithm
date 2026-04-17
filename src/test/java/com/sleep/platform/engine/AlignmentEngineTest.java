package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.model.MotionAlignmentResult;
import com.sleep.platform.domain.request.MotionSegmentInput;
import com.sleep.platform.domain.request.PhysiologicalSegmentInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class AlignmentEngineTest {

    @Test
    void shouldAlignFiveMinutePhysWithEightMinuteMotion() {
        SleepAnalysisProperties properties = properties();
        MultiWindowAlignmentEngine engine = new MultiWindowAlignmentEngine(properties);
        PhysiologicalSegmentInput physiological = new PhysiologicalSegmentInput();
        physiological.setSegmentStartTime(LocalDateTime.of(2026, 4, 1, 23, 0));
        MotionSegmentInput motion = new MotionSegmentInput();
        motion.setMotionSegmentStartTime(LocalDateTime.of(2026, 4, 1, 22, 58));
        motion.setStepsInEightMinutes(80);

        MotionAlignmentResult result = engine.alignSingleSegment(physiological, List.of(motion));
        Assertions.assertTrue(result.getAlignedStepsInFiveMinutes() > 0);
        Assertions.assertTrue(result.getMotionAlignmentConfidence() > 0.5);
    }

    @Test
    void shouldOutputLowAlignmentConfidenceWhenMotionFarAway() {
        SleepAnalysisProperties properties = properties();
        MultiWindowAlignmentEngine engine = new MultiWindowAlignmentEngine(properties);
        PhysiologicalSegmentInput physiological = new PhysiologicalSegmentInput();
        physiological.setSegmentStartTime(LocalDateTime.of(2026, 4, 1, 23, 0));
        MotionSegmentInput motion = new MotionSegmentInput();
        motion.setMotionSegmentStartTime(LocalDateTime.of(2026, 4, 1, 20, 0));
        motion.setStepsInEightMinutes(120);

        MotionAlignmentResult result = engine.alignSingleSegment(physiological, List.of(motion));
        Assertions.assertEquals(0.0, result.getMotionAlignmentConfidence());
    }

    private SleepAnalysisProperties properties() {
        SleepAnalysisProperties p = new SleepAnalysisProperties();
        p.setMotionAlignmentLookbackMinutes(8);
        p.setMotionAlignmentLookaheadMinutes(8);
        return p;
    }
}
