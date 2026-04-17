package com.sleep.platform.feature;

import com.sleep.platform.domain.model.SleepTransitionFeatures;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FeatureExplainTagBuilder {

    public List<String> buildTags(SleepTransitionFeatures features) {
        List<String> tags = new ArrayList<>();
        if (features.getMotionAlignmentConfidence() < 0.4) {
            tags.add("LOW_ALIGNMENT_CONFIDENCE");
            tags.add("MOTION_ONLY_WEAK_EVIDENCE");
        }
        if (features.isLowMotionSegment()) {
            tags.add("ALIGNED_LOW_MOTION");
            tags.add("MOTION_SUPPORTS_SLEEP");
        }
        if (features.isHighMotionSegment()) {
            tags.add("ALIGNED_HIGH_MOTION");
            tags.add("MOTION_SUPPORTS_WAKE");
        }
        if (features.isSleepStageCandidate()) {
            tags.add("SLEEP_STAGE_CANDIDATE");
        }
        if (features.isWakeStageCandidate()) {
            tags.add("WAKE_STAGE_CANDIDATE");
        }
        if (features.isDaytimeNapCandidate()) {
            tags.add("DAYTIME_NAP_CANDIDATE");
        }
        return tags;
    }
}
