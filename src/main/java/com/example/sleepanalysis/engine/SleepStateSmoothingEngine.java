package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.domain.response.SleepStageTimelineItem;
import com.example.sleepanalysis.enums.SleepStage;

import java.util.ArrayList;
import java.util.List;

/**
 * 睡眠状态平滑引擎（迟滞机制）。
 * <p>
 * 状态切换必须连续 2 段确认。
 * </p>
 */
public class SleepStateSmoothingEngine {

    /**
     * 对时间轴进行状态平滑。
     *
     * @param timeline 原始时间轴
     * @return 平滑后的时间轴
     */
    public List<SleepStageTimelineItem> smooth(List<SleepStageTimelineItem> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return new ArrayList<>();
        }

        List<SleepStageTimelineItem> smoothed = new ArrayList<>(timeline.size());

        SleepStage stableStage = timeline.get(0).getSleepStage();
        SleepStage candidateStage = stableStage;
        int candidateCount = 0;

        for (int i = 0; i < timeline.size(); i++) {
            SleepStageTimelineItem source = timeline.get(i);
            SleepStage predicted = source.getSleepStage();

            if (predicted == stableStage) {
                candidateStage = stableStage;
                candidateCount = 0;
            } else {
                if (predicted == candidateStage) {
                    candidateCount++;
                } else {
                    candidateStage = predicted;
                    candidateCount = 1;
                }

                if (candidateCount >= 2) {
                    stableStage = candidateStage;
                    candidateCount = 0;
                }
            }

            SleepStageTimelineItem target = new SleepStageTimelineItem();
            target.setSegmentId(source.getSegmentId());
            target.setStageStartTime(source.getStageStartTime());
            target.setStageEndTime(source.getStageEndTime());
            target.setStageConfidence(source.getStageConfidence());
            target.setSleepStage(stableStage);
            smoothed.add(target);
        }

        return smoothed;
    }
}
