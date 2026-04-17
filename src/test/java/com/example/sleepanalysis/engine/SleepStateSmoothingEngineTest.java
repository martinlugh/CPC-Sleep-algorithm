package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.domain.response.SleepStageTimelineItem;
import com.example.sleepanalysis.enums.SleepStage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SleepStateSmoothingEngine 单元测试。
 */
public class SleepStateSmoothingEngineTest {

    @Test
    void shouldSwitchOnlyAfterTwoConsecutiveSegments() {
        SleepStateSmoothingEngine engine = new SleepStateSmoothingEngine();

        List<SleepStageTimelineItem> raw = new ArrayList<>();
        raw.add(item("s1", SleepStage.LIGHT, "2026-04-10T22:00:00+08:00", "2026-04-10T22:00:30+08:00"));
        raw.add(item("s2", SleepStage.DEEP, "2026-04-10T22:00:30+08:00", "2026-04-10T22:01:00+08:00"));
        raw.add(item("s3", SleepStage.DEEP, "2026-04-10T22:01:00+08:00", "2026-04-10T22:01:30+08:00"));

        List<SleepStageTimelineItem> smoothed = engine.smooth(raw);

        Assertions.assertEquals(SleepStage.LIGHT, smoothed.get(0).getSleepStage());
        Assertions.assertEquals(SleepStage.LIGHT, smoothed.get(1).getSleepStage());
        Assertions.assertEquals(SleepStage.DEEP, smoothed.get(2).getSleepStage());
    }

    private SleepStageTimelineItem item(String id, SleepStage stage, String start, String end) {
        SleepStageTimelineItem item = new SleepStageTimelineItem();
        item.setSegmentId(id);
        item.setSleepStage(stage);
        item.setStageStartTime(OffsetDateTime.parse(start));
        item.setStageEndTime(OffsetDateTime.parse(end));
        item.setStageConfidence(0.8D);
        return item;
    }
}
