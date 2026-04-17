package com.example.sleepanalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 睡眠分析模块配置属性。
 */
@ConfigurationProperties(prefix = "sleep.analysis")
public class SleepAnalysisProperties {

    /** 采样率（Hz）。 */
    private int sampleRateHz;

    /** 最小时段长度（秒）。 */
    private int minSegmentSeconds;

    /** 最大时段长度（秒）。 */
    private int maxSegmentSeconds;

    /** 是否启用基线自适应。 */
    private boolean enableBaselineAdaptation;

    /** 默认时区。 */
    private String defaultTimeZone;

    public int getSampleRateHz() {
        return sampleRateHz;
    }

    public void setSampleRateHz(int sampleRateHz) {
        this.sampleRateHz = sampleRateHz;
    }

    public int getMinSegmentSeconds() {
        return minSegmentSeconds;
    }

    public void setMinSegmentSeconds(int minSegmentSeconds) {
        this.minSegmentSeconds = minSegmentSeconds;
    }

    public int getMaxSegmentSeconds() {
        return maxSegmentSeconds;
    }

    public void setMaxSegmentSeconds(int maxSegmentSeconds) {
        this.maxSegmentSeconds = maxSegmentSeconds;
    }

    public boolean isEnableBaselineAdaptation() {
        return enableBaselineAdaptation;
    }

    public void setEnableBaselineAdaptation(boolean enableBaselineAdaptation) {
        this.enableBaselineAdaptation = enableBaselineAdaptation;
    }

    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }
}
