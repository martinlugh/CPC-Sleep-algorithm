package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.model.MotionAlignmentResult;
import com.sleep.platform.domain.model.PhysiologicalMotionMergedSegment;
import com.sleep.platform.domain.request.MotionSegmentInput;
import com.sleep.platform.domain.request.PhysiologicalSegmentInput;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class MultiWindowAlignmentEngine {

    private static final int PHYSIOLOGICAL_MINUTES = 5;
    private static final int MOTION_MINUTES = 8;

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public MultiWindowAlignmentEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public List<PhysiologicalMotionMergedSegment> align(List<PhysiologicalSegmentInput> physiologicalSegmentList,
                                                        List<MotionSegmentInput> motionSegmentList) {
        List<PhysiologicalSegmentInput> sortedPhys = physiologicalSegmentList.stream()
                .sorted(Comparator.comparing(PhysiologicalSegmentInput::getSegmentStartTime))
                .toList();
        List<MotionSegmentInput> sortedMotion = motionSegmentList.stream()
                .sorted(Comparator.comparing(MotionSegmentInput::getMotionSegmentStartTime))
                .toList();

        List<PhysiologicalMotionMergedSegment> mergedList = new ArrayList<>();
        for (PhysiologicalSegmentInput physiologicalSegment : sortedPhys) {
            MotionAlignmentResult alignmentResult = alignSingleSegment(physiologicalSegment, sortedMotion);
            PhysiologicalMotionMergedSegment mergedSegment = new PhysiologicalMotionMergedSegment();
            mergedSegment.setSegmentStartTime(physiologicalSegment.getSegmentStartTime());
            mergedSegment.setAverageHeartRateBpm(physiologicalSegment.getAverageHeartRateBpm());
            mergedSegment.setAlignedStepsInFiveMinutes(alignmentResult.getAlignedStepsInFiveMinutes());
            mergedSegment.setMotionAlignmentConfidence(alignmentResult.getMotionAlignmentConfidence());
            mergedSegment.setAlignmentReason(alignmentResult.getAlignmentReason());
            mergedList.add(mergedSegment);
        }
        return mergedList;
    }

    public MotionAlignmentResult alignSingleSegment(PhysiologicalSegmentInput physiologicalSegment,
                                                    List<MotionSegmentInput> motionSegmentList) {
        MotionAlignmentResult result = new MotionAlignmentResult();
        LocalDateTime physiologicalStart = physiologicalSegment.getSegmentStartTime();
        LocalDateTime physiologicalEnd = physiologicalStart.plusMinutes(PHYSIOLOGICAL_MINUTES);

        // 由于运动数据与生理数据时间窗不同，本系统采用工程化时间对齐策略，将8分钟运动数据映射为5分钟主时间轴上的辅助特征，该结果用于增强边界识别，但不作为唯一判定依据。

        MotionSegmentInput bestOverlapSegment = null;
        long bestOverlapSeconds = 0;
        for (MotionSegmentInput motionSegment : motionSegmentList) {
            LocalDateTime motionStart = motionSegment.getMotionSegmentStartTime();
            LocalDateTime motionEnd = motionStart.plusMinutes(MOTION_MINUTES);
            long overlap = overlapSeconds(physiologicalStart, physiologicalEnd, motionStart, motionEnd);
            if (overlap > bestOverlapSeconds) {
                bestOverlapSeconds = overlap;
                bestOverlapSegment = motionSegment;
            }
        }

        if (bestOverlapSegment != null && bestOverlapSeconds > 0) {
            double overlapRatioOnMotion = bestOverlapSeconds / (MOTION_MINUTES * 60.0);
            double alignedSteps = bestOverlapSegment.getStepsInEightMinutes() * overlapRatioOnMotion;
            double confidence = Math.min(1.0, 0.55 + bestOverlapSeconds / (PHYSIOLOGICAL_MINUTES * 60.0) * 0.45);
            result.setAlignedStepsInFiveMinutes(alignedSteps);
            result.setMotionAlignmentConfidence(confidence);
            result.setAlignmentReason("OVERLAP_MAPPING");
            result.setMatchedMotionSegmentStartTime(bestOverlapSegment.getMotionSegmentStartTime());
            return result;
        }

        MotionSegmentInput nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (MotionSegmentInput motionSegment : motionSegmentList) {
            LocalDateTime motionStart = motionSegment.getMotionSegmentStartTime();
            LocalDateTime motionEnd = motionStart.plusMinutes(MOTION_MINUTES);
            long distance = intervalDistanceSeconds(physiologicalStart, physiologicalEnd, motionStart, motionEnd);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = motionSegment;
            }
        }

        long lookbackSeconds = sleepAnalysisProperties.getMotionAlignmentLookbackMinutes() * 60L;
        long lookaheadSeconds = sleepAnalysisProperties.getMotionAlignmentLookaheadMinutes() * 60L;
        long maxAllowed = Math.max(lookbackSeconds, lookaheadSeconds);
        if (nearest != null && nearestDistance <= maxAllowed) {
            double proximity = 1.0 - Math.min(1.0, nearestDistance * 1.0 / Math.max(1.0, maxAllowed));
            double alignedSteps = nearest.getStepsInEightMinutes() * (PHYSIOLOGICAL_MINUTES * 1.0 / MOTION_MINUTES) * proximity;
            double confidence = 0.25 + 0.45 * proximity;
            result.setAlignedStepsInFiveMinutes(alignedSteps);
            result.setMotionAlignmentConfidence(confidence);
            result.setAlignmentReason("NEAREST_NEIGHBOR_MAPPING");
            result.setMatchedMotionSegmentStartTime(nearest.getMotionSegmentStartTime());
            return result;
        }

        result.setAlignedStepsInFiveMinutes(0.0);
        result.setMotionAlignmentConfidence(0.0);
        result.setAlignmentReason("NO_MOTION_MATCH");
        return result;
    }

    private long overlapSeconds(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime overlapStart = start1.isAfter(start2) ? start1 : start2;
        LocalDateTime overlapEnd = end1.isBefore(end2) ? end1 : end2;
        if (!overlapEnd.isAfter(overlapStart)) {
            return 0L;
        }
        return Duration.between(overlapStart, overlapEnd).getSeconds();
    }

    private long intervalDistanceSeconds(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        if (!end1.isBefore(start2) && !end2.isBefore(start1)) {
            return 0L;
        }
        if (end1.isBefore(start2)) {
            return Duration.between(end1, start2).getSeconds();
        }
        return Duration.between(end2, start1).getSeconds();
    }
}
