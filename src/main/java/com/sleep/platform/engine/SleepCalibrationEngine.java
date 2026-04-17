package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.QualityGateResult;
import com.sleep.platform.domain.model.SleepCalibrationResult;
import org.springframework.stereotype.Component;

@Component
public class SleepCalibrationEngine {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public SleepCalibrationEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public SleepCalibrationResult calibrate(SleepStage stageBeforeCalibration,
                                            double confidenceScore,
                                            double averageHeartRateBpm,
                                            double baselineRestingHr,
                                            double sampleEntropy,
                                            double sd2Sd1Ratio,
                                            double motionAlignedSteps,
                                            QualityGateResult qualityGateResult,
                                            SleepStage previousSmoothedStage) {
        SleepCalibrationResult result = new SleepCalibrationResult();
        SleepStage calibrated = stageBeforeCalibration;
        double calibratedConfidence = confidenceScore;

        double motionWeight = qualityGateResult.getMotionAlignmentConfidence() < 0.5
                ? qualityGateResult.getMotionAlignmentConfidence() * sleepAnalysisProperties.getLowAlignmentConfidenceMotionWeight()
                : qualityGateResult.getMotionAlignmentConfidence();

        if (stageBeforeCalibration == SleepStage.WAKE && motionWeight < 0.25
                && averageHeartRateBpm <= baselineRestingHr + 3.0
                && sampleEntropy < sleepAnalysisProperties.getRemSampleEntropyLowerThreshold()) {
            calibrated = SleepStage.LIGHT;
            calibratedConfidence = Math.max(0.45, confidenceScore - 0.12);
        }

        if (stageBeforeCalibration == SleepStage.DEEP && sampleEntropy > sleepAnalysisProperties.getDeepSampleEntropyUpperThreshold()) {
            calibrated = SleepStage.LIGHT;
            calibratedConfidence = Math.max(0.40, confidenceScore - 0.15);
        }

        if (stageBeforeCalibration == SleepStage.REM && sd2Sd1Ratio < sleepAnalysisProperties.getRemSd2Sd1RatioThreshold()) {
            calibrated = SleepStage.LIGHT;
            calibratedConfidence = Math.max(0.40, confidenceScore - 0.10);
        }

        if (stageBeforeCalibration == SleepStage.LIGHT
                && motionAlignedSteps >= sleepAnalysisProperties.getWakeUpAlignedStepThreshold()
                && motionWeight >= 0.45
                && averageHeartRateBpm > baselineRestingHr + 7.0) {
            calibrated = SleepStage.WAKE;
            calibratedConfidence = Math.min(0.90, confidenceScore + 0.12);
        }

        SleepStage smoothed = calibrated;
        if (previousSmoothedStage != null && previousSmoothedStage != SleepStage.UNKNOWN) {
            if (calibrated == SleepStage.WAKE && previousSmoothedStage == SleepStage.DEEP && calibratedConfidence < 0.8) {
                smoothed = SleepStage.LIGHT;
            }
            if (calibrated == SleepStage.DEEP && previousSmoothedStage == SleepStage.WAKE && calibratedConfidence < 0.8) {
                smoothed = SleepStage.LIGHT;
            }
        }

        result.setStageAfterCalibration(calibrated);
        result.setSmoothedStage(smoothed);
        result.setConfidenceScore(calibratedConfidence);
        return result;
    }
}
