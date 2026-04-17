package com.example.sleepanalysis.domain.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 单个睡眠片段输入数据。
 */
public class SleepSegmentInput {

    /** 片段唯一标识。 */
    @NotNull
    private String segmentId;

    /** 片段开始时间。 */
    @NotNull
    private OffsetDateTime segmentStartTime;

    /** 片段结束时间。 */
    @NotNull
    private OffsetDateTime segmentEndTime;

    /** 心率序列（bpm）。 */
    @NotNull
    private List<Double> heartRateSeries;

    /** 呼吸率序列（rpm）。 */
    @NotNull
    private List<Double> respirationRateSeries;

    /** 体动强度序列。 */
    @NotNull
    private List<Double> bodyMovementSeries;

    /** 血氧序列（%）。 */
    private List<Double> spo2Series;

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public OffsetDateTime getSegmentStartTime() {
        return segmentStartTime;
    }

    public void setSegmentStartTime(OffsetDateTime segmentStartTime) {
        this.segmentStartTime = segmentStartTime;
    }

    public OffsetDateTime getSegmentEndTime() {
        return segmentEndTime;
    }

    public void setSegmentEndTime(OffsetDateTime segmentEndTime) {
        this.segmentEndTime = segmentEndTime;
    }

    public List<Double> getHeartRateSeries() {
        return heartRateSeries;
    }

    public void setHeartRateSeries(List<Double> heartRateSeries) {
        this.heartRateSeries = heartRateSeries;
    }

    public List<Double> getRespirationRateSeries() {
        return respirationRateSeries;
    }

    public void setRespirationRateSeries(List<Double> respirationRateSeries) {
        this.respirationRateSeries = respirationRateSeries;
    }

    public List<Double> getBodyMovementSeries() {
        return bodyMovementSeries;
    }

    public void setBodyMovementSeries(List<Double> bodyMovementSeries) {
        this.bodyMovementSeries = bodyMovementSeries;
    }

    public List<Double> getSpo2Series() {
        return spo2Series;
    }

    public void setSpo2Series(List<Double> spo2Series) {
        this.spo2Series = spo2Series;
    }
}
