package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.CpcAnalysisResult;
import com.sleep.platform.domain.model.QualityGateResult;
import com.sleep.platform.domain.model.SleepDecisionResult;
import org.springframework.stereotype.Component;

@Component
public class SleepDecisionEngine {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public SleepDecisionEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public SleepDecisionResult decideInitialStage(QualityGateResult qualityGateResult,
                                                  CpcAnalysisResult cpcAnalysisResult,
                                                  double averageHeartRateBpm,
                                                  double sampleEntropy,
                                                  double sd2Sd1Ratio,
                                                  double motionAlignedSteps,
                                                  double baselineRestingHr) {
        SleepDecisionResult result = new SleepDecisionResult();
        if (!qualityGateResult.isQualityPassed()) {
            result.setStageBeforeCalibration(SleepStage.UNKNOWN);
            result.setConfidenceScore(0.2);
            return result;
        }

        double motionConfidence = qualityGateResult.getMotionAlignmentConfidence();
        double motionWeight = motionConfidence < 0.5
                ? motionConfidence * sleepAnalysisProperties.getLowAlignmentConfidenceMotionWeight()
                : motionConfidence;

        boolean wakeByMotion = motionAlignedSteps >= sleepAnalysisProperties.getWakeStepThreshold() && motionWeight >= 0.35;
        boolean wakeByPhysiology = averageHeartRateBpm >= baselineRestingHr + 8.0
                || sampleEntropy >= sleepAnalysisProperties.getRemSampleEntropyLowerThreshold() + 0.25;

        if (wakeByMotion && wakeByPhysiology) {
            result.setStageBeforeCalibration(SleepStage.WAKE);
            result.setConfidenceScore(0.82);
            return result;
        }

        boolean deepSignal = cpcAnalysisResult.getHfcPower() > cpcAnalysisResult.getLfcPower()
                && cpcAnalysisResult.getHfcLfcRatio() >= sleepAnalysisProperties.getDeepHfcLfcRatioThreshold()
                && averageHeartRateBpm <= baselineRestingHr + 3.0;
        if (deepSignal) {
            double confidence = 0.74 + Math.min(0.2, cpcAnalysisResult.getHfcLfcRatio() / 5.0);
            if (motionAlignedSteps <= sleepAnalysisProperties.getSleepOnsetMaxAlignedStepThreshold()) {
                confidence += 0.04 * motionWeight;
            }
            result.setStageBeforeCalibration(SleepStage.DEEP);
            result.setConfidenceScore(Math.min(0.95, confidence));
            return result;
        }

        boolean remSignal = sd2Sd1Ratio >= sleepAnalysisProperties.getRemSd2Sd1RatioThreshold()
                && sampleEntropy >= sleepAnalysisProperties.getRemSampleEntropyLowerThreshold();
        if (remSignal) {
            result.setStageBeforeCalibration(SleepStage.REM);
            result.setConfidenceScore(0.70);
            return result;
        }

        boolean lightSignal = cpcAnalysisResult.getLfcPower() >= cpcAnalysisResult.getHfcPower() * 0.8;
        if (lightSignal) {
            result.setStageBeforeCalibration(SleepStage.LIGHT);
            result.setConfidenceScore(0.64);
            return result;
        }

        if (wakeByPhysiology) {
            result.setStageBeforeCalibration(SleepStage.WAKE);
            result.setConfidenceScore(0.60);
            return result;
        }

        result.setStageBeforeCalibration(SleepStage.LIGHT);
        result.setConfidenceScore(0.55);
        return result;
    }
}
