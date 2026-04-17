package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.response.DaytimeNapResultItem;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import com.sleep.platform.domain.response.SleepStageTimelineItem;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DaytimeNapDetectionEngine {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public DaytimeNapDetectionEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public List<DaytimeNapResultItem> detect(List<SleepSegmentAnalysisResult> segmentList) {
        if (segmentList == null || segmentList.isEmpty()) {
            return Collections.emptyList();
        }
        List<DaytimeNapResultItem> napResultList = new ArrayList<>();
        int minSegments = Math.max(1, sleepAnalysisProperties.getDaytimeNapRequiredSegments());

        int i = 0;
        while (i < segmentList.size()) {
            SleepSegmentAnalysisResult current = segmentList.get(i);
            int hour = current.getSegmentStartTime().getHour();
            boolean daytime = hour >= 9 && hour < 20;
            boolean sleepLike = isSleepLike(current.getSmoothedStage()) && Boolean.TRUE.equals(current.getQualityPassed());
            if (!daytime || !sleepLike) {
                i++;
                continue;
            }

            int start = i;
            int end = i;
            while (end + 1 < segmentList.size()) {
                SleepSegmentAnalysisResult next = segmentList.get(end + 1);
                if (!isSleepLike(next.getSmoothedStage())) {
                    break;
                }
                if (Duration.between(segmentList.get(end).getSegmentStartTime(), next.getSegmentStartTime()).toMinutes()
                        > sleepAnalysisProperties.getDaytimeNapMergeGapMinutes()) {
                    break;
                }
                end++;
            }

            int segmentCount = end - start + 1;
            int totalMinutes = segmentCount * 5;
            if (segmentCount >= minSegments && totalMinutes >= sleepAnalysisProperties.getDaytimeNapMinMinutes()) {
                DaytimeNapResultItem nap = new DaytimeNapResultItem();
                nap.setNapStartTime(segmentList.get(start).getSegmentStartTime());
                nap.setNapEndTime(segmentList.get(end).getSegmentStartTime().plusMinutes(5));
                nap.setNapTotalMinutes(totalMinutes);
                nap.setNapStageTimeline(buildTimeline(segmentList, start, end));
                napResultList.add(nap);
            }
            i = end + 1;
        }
        return napResultList;
    }

    private boolean isSleepLike(SleepStage stage) {
        return stage == SleepStage.LIGHT || stage == SleepStage.DEEP || stage == SleepStage.REM;
    }

    private List<SleepStageTimelineItem> buildTimeline(List<SleepSegmentAnalysisResult> source, int start, int end) {
        List<SleepStageTimelineItem> timeline = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            SleepSegmentAnalysisResult item = source.get(i);
            SleepStageTimelineItem timelineItem = new SleepStageTimelineItem();
            timelineItem.setSegmentStartTime(item.getSegmentStartTime());
            timelineItem.setStage(item.getSmoothedStage());
            timelineItem.setConfidenceScore(item.getConfidenceScore());
            timeline.add(timelineItem);
        }
        return timeline;
    }
}
