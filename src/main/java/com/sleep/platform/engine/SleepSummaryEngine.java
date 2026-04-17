package com.sleep.platform.engine;

import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.MainSleepSummaryResult;
import com.sleep.platform.domain.model.SleepOnsetDetectionResult;
import com.sleep.platform.domain.model.WakeUpDetectionResult;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SleepSummaryEngine {

    public MainSleepSummaryResult summarize(List<SleepSegmentAnalysisResult> smoothedSegments,
                                            SleepOnsetDetectionResult onsetResult,
                                            WakeUpDetectionResult wakeUpResult) {
        List<SleepSegmentAnalysisResult> safeSegments = smoothedSegments == null ? Collections.emptyList() : smoothedSegments;
        MainSleepSummaryResult summary = new MainSleepSummaryResult();
        LocalDateTime start = onsetResult == null ? null : onsetResult.getMainSleepStartTime();
        LocalDateTime wake = wakeUpResult == null ? null : wakeUpResult.getMainSleepWakeUpTime();
        summary.setMainSleepStartTime(start);
        summary.setMainSleepWakeUpTime(wake);
        summary.setMainSleepLatencyMinutes(onsetResult == null ? null : onsetResult.getMainSleepLatencyMinutes());

        int deepMinutes = 0;
        int lightMinutes = 0;
        int remMinutes = 0;
        int awakeMinutes = 0;
        int awakenCount = 0;
        boolean inWakeBlock = false;

        for (SleepSegmentAnalysisResult segment : safeSegments) {
            if (segment == null || segment.getSegmentStartTime() == null) {
                continue;
            }
            if (start != null && segment.getSegmentStartTime().isBefore(start)) {
                continue;
            }
            if (wake != null && !segment.getSegmentStartTime().isBefore(wake)) {
                continue;
            }
            SleepStage stage = segment.getSmoothedStage();
            if (stage == SleepStage.DEEP) {
                deepMinutes += 5;
                inWakeBlock = false;
            } else if (stage == SleepStage.LIGHT) {
                lightMinutes += 5;
                inWakeBlock = false;
            } else if (stage == SleepStage.REM) {
                remMinutes += 5;
                inWakeBlock = false;
            } else if (stage == SleepStage.WAKE) {
                awakeMinutes += 5;
                if (!inWakeBlock) {
                    awakenCount++;
                    inWakeBlock = true;
                }
            }
        }

        int totalMinutes = deepMinutes + lightMinutes + remMinutes;
        int observedWindowMinutes = totalMinutes + awakeMinutes;
        int windowMinutes = (start != null && wake != null)
                ? Math.max(5, (int) Duration.between(start, wake).toMinutes())
                : Math.max(5, observedWindowMinutes);
        double efficiency = windowMinutes == 0 ? 0.0 : totalMinutes * 1.0 / windowMinutes;

        summary.setMainSleepDeepMinutes(deepMinutes);
        summary.setMainSleepLightMinutes(lightMinutes);
        summary.setMainSleepRemMinutes(remMinutes);
        summary.setMainSleepAwakeMinutes(awakeMinutes);
        summary.setMainSleepTotalMinutes(totalMinutes);
        summary.setMainSleepAwakenCount(Math.max(0, awakenCount - 1));
        summary.setMainSleepEfficiency(efficiency);

        List<String> tags = new ArrayList<>();
        tags.add("PHYSIOLOGY_PRIMARY_SUMMARY");
        tags.add("MOTION_CONFLICT_RESOLVED_BY_PHYSIOLOGY");
        summary.setSummaryExplainTags(tags);
        summary.setSummaryExplainText("主睡眠汇总基于5分钟生理主时间轴计算，运动证据仅用于增强和反证，冲突时优先采用生理证据。");
        return summary;
    }
}
