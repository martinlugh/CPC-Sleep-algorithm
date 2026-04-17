package com.example.sleepanalysis.domain.response;

import com.example.sleepanalysis.enums.SleepStage;

import java.time.OffsetDateTime;

/**
 * 睡眠分期时间轴项。
 */
public class SleepStageTimelineItem {

    /** 片段唯一标识。 */
    private String segmentId;

    /** 阶段开始时间。 */
    private OffsetDateTime stageStartTime;

    /** 阶段结束时间。 */
    private OffsetDateTime stageEndTime;

    /** 睡眠阶段。 */
    private SleepStage sleepStage;

    /** 阶段置信度（0~1）。 */
    private Double stageConfidence;

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public OffsetDateTime getStageStartTime() {
        return stageStartTime;
    }

    public void setStageStartTime(OffsetDateTime stageStartTime) {
        this.stageStartTime = stageStartTime;
    }

    public OffsetDateTime getStageEndTime() {
        return stageEndTime;
    }

    public void setStageEndTime(OffsetDateTime stageEndTime) {
        this.stageEndTime = stageEndTime;
    }

    public SleepStage getSleepStage() {
        return sleepStage;
    }

    public void setSleepStage(SleepStage sleepStage) {
        this.sleepStage = sleepStage;
    }

    public Double getStageConfidence() {
        return stageConfidence;
    }

    public void setStageConfidence(Double stageConfidence) {
        this.stageConfidence = stageConfidence;
    }
}
