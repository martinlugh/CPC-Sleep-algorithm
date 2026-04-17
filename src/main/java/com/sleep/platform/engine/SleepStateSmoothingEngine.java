package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SleepStateSmoothingEngine {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public SleepStateSmoothingEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public List<SleepSegmentAnalysisResult> smooth(List<SleepSegmentAnalysisResult> segmentResultList) {
        List<SleepSegmentAnalysisResult> output = new ArrayList<>(segmentResultList);
        int hysteresis = Math.max(1, sleepAnalysisProperties.getHysteresisRequiredSegments());
        for (int i = 1; i < output.size() - 1; i++) {
            SleepSegmentAnalysisResult prev = output.get(i - 1);
            SleepSegmentAnalysisResult curr = output.get(i);
            SleepSegmentAnalysisResult next = output.get(i + 1);
            if (prev.getStageAfterCalibration() == next.getStageAfterCalibration()
                    && curr.getStageAfterCalibration() != prev.getStageAfterCalibration()) {
                curr.setSmoothedStage(prev.getStageAfterCalibration());
            } else {
                curr.setSmoothedStage(curr.getStageAfterCalibration());
            }

            if (i >= hysteresis) {
                int wakeCount = 0;
                int sleepCount = 0;
                for (int j = i - hysteresis + 1; j <= i; j++) {
                    SleepStage stage = output.get(j).getSmoothedStage();
                    if (stage == SleepStage.WAKE) {
                        wakeCount++;
                    } else if (stage == SleepStage.LIGHT || stage == SleepStage.DEEP || stage == SleepStage.REM) {
                        sleepCount++;
                    }
                }
                if (wakeCount == hysteresis) {
                    curr.setSmoothedStage(SleepStage.WAKE);
                }
                if (sleepCount == hysteresis && curr.getSmoothedStage() == SleepStage.WAKE
                        && curr.getConfidenceScore() != null && curr.getConfidenceScore() < 0.75) {
                    curr.setSmoothedStage(SleepStage.LIGHT);
                }
            }
        }
        if (!output.isEmpty()) {
            output.get(0).setSmoothedStage(output.get(0).getStageAfterCalibration());
            if (output.size() > 1) {
                output.get(output.size() - 1).setSmoothedStage(output.get(output.size() - 1).getStageAfterCalibration());
            }
        }
        return output;
    }
}
