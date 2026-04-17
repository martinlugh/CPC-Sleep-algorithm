package com.sleep.platform.feature;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.model.PhysiologicalMotionMergedSegment;
import com.sleep.platform.domain.model.SleepTransitionFeatures;
import com.sleep.platform.domain.model.UserSleepBaselineProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SleepTransitionFeatureExtractor {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public SleepTransitionFeatureExtractor(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public List<SleepTransitionFeatures> extractFeatures(List<PhysiologicalMotionMergedSegment> mergedSegmentList,
                                                         UserSleepBaselineProfile baselineProfile) {
        List<SleepTransitionFeatures> output = new ArrayList<>();
        int consecutiveLowMotionCount = 0;
        int consecutiveWakeLikeCount = 0;
        int consecutiveSleepLikeCount = 0;
        int consecutiveDaytimeNapLikeCount = 0;

        for (int i = 0; i < mergedSegmentList.size(); i++) {
            PhysiologicalMotionMergedSegment current = mergedSegmentList.get(i);
            PhysiologicalMotionMergedSegment previous = i > 0 ? mergedSegmentList.get(i - 1) : null;

            SleepTransitionFeatures features = new SleepTransitionFeatures();
            features.setSegmentStartTime(current.getSegmentStartTime());
            features.setMotionAlignmentConfidence(current.getMotionAlignmentConfidence());

            boolean lowMotion = current.getAlignedStepsInFiveMinutes() <= sleepAnalysisProperties.getSleepOnsetMaxAlignedStepThreshold();
            boolean highMotion = current.getAlignedStepsInFiveMinutes() >= sleepAnalysisProperties.getWakeUpAlignedStepThreshold();
            features.setLowMotionSegment(lowMotion);
            features.setHighMotionSegment(highMotion);

            double restingHr = baselineProfile != null && baselineProfile.getRestingHeartRateBpm() != null
                    ? baselineProfile.getRestingHeartRateBpm() : current.getAverageHeartRateBpm();
            boolean nearBaseline = Math.abs(current.getAverageHeartRateBpm() - restingHr) <= 6.0;
            features.setHeartRateNearNightBaseline(nearBaseline);

            boolean hrRise = previous != null && (current.getAverageHeartRateBpm() - previous.getAverageHeartRateBpm()) >= sleepAnalysisProperties.getWakeUpHeartRateDeltaThreshold();
            boolean hrDrop = previous != null && (current.getAverageHeartRateBpm() - previous.getAverageHeartRateBpm()) <= sleepAnalysisProperties.getSleepOnsetHeartRateDeltaThreshold();
            features.setHeartRateRiseComparedToPrevious(hrRise);
            features.setHeartRateDropComparedToPrevious(hrDrop);

            boolean sleepLike = lowMotion && (nearBaseline || hrDrop);
            boolean wakeLike = highMotion || hrRise;
            boolean daytimeNapLike = lowMotion && !highMotion;
            features.setSleepStageCandidate(sleepLike);
            features.setWakeStageCandidate(wakeLike);
            features.setDaytimeNapCandidate(daytimeNapLike);

            consecutiveLowMotionCount = lowMotion ? consecutiveLowMotionCount + 1 : 0;
            consecutiveWakeLikeCount = wakeLike ? consecutiveWakeLikeCount + 1 : 0;
            consecutiveSleepLikeCount = sleepLike ? consecutiveSleepLikeCount + 1 : 0;
            consecutiveDaytimeNapLikeCount = daytimeNapLike ? consecutiveDaytimeNapLikeCount + 1 : 0;

            features.setConsecutiveLowMotionCount(consecutiveLowMotionCount);
            features.setConsecutiveWakeLikeCount(consecutiveWakeLikeCount);
            features.setConsecutiveSleepLikeCount(consecutiveSleepLikeCount);
            features.setConsecutiveDaytimeNapLikeCount(consecutiveDaytimeNapLikeCount);

            double motionWeight = current.getMotionAlignmentConfidence();
            if (current.getMotionAlignmentConfidence() < 0.5) {
                motionWeight = motionWeight * sleepAnalysisProperties.getLowAlignmentConfidenceMotionWeight();
            }
            features.setMotionEvidenceWeight(motionWeight);
            features.setPhysiologicalEvidenceWeight(Math.max(0.0, 1.0 - motionWeight * 0.5));
            output.add(features);
        }
        return output;
    }
}
