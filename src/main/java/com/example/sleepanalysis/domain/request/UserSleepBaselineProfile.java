package com.example.sleepanalysis.domain.request;

import java.util.List;

/**
 * 用户睡眠基线档案。
 */
public class UserSleepBaselineProfile {

    /** 用户唯一标识。 */
    private String userId;

    /** 基线静息心率（bpm）。 */
    private Double baselineRestingHeartRate;

    /** 基线呼吸率（rpm）。 */
    private Double baselineRespirationRate;

    /** 基线血氧（%）。 */
    private Double baselineSpo2;

    /** 历史平均入睡时长（分钟）。 */
    private Double averageSleepLatencyMinutes;

    /** 历史平均总睡眠时长（分钟）。 */
    private Double averageTotalSleepMinutes;

    /** 历史常见入睡时刻（HH:mm）。 */
    private String habitualSleepStartClock;

    /** 历史常见醒来时刻（HH:mm）。 */
    private String habitualWakeUpClock;

    /** 用户标签。 */
    private List<String> profileTags;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getBaselineRestingHeartRate() {
        return baselineRestingHeartRate;
    }

    public void setBaselineRestingHeartRate(Double baselineRestingHeartRate) {
        this.baselineRestingHeartRate = baselineRestingHeartRate;
    }

    public Double getBaselineRespirationRate() {
        return baselineRespirationRate;
    }

    public void setBaselineRespirationRate(Double baselineRespirationRate) {
        this.baselineRespirationRate = baselineRespirationRate;
    }

    public Double getBaselineSpo2() {
        return baselineSpo2;
    }

    public void setBaselineSpo2(Double baselineSpo2) {
        this.baselineSpo2 = baselineSpo2;
    }

    public Double getAverageSleepLatencyMinutes() {
        return averageSleepLatencyMinutes;
    }

    public void setAverageSleepLatencyMinutes(Double averageSleepLatencyMinutes) {
        this.averageSleepLatencyMinutes = averageSleepLatencyMinutes;
    }

    public Double getAverageTotalSleepMinutes() {
        return averageTotalSleepMinutes;
    }

    public void setAverageTotalSleepMinutes(Double averageTotalSleepMinutes) {
        this.averageTotalSleepMinutes = averageTotalSleepMinutes;
    }

    public String getHabitualSleepStartClock() {
        return habitualSleepStartClock;
    }

    public void setHabitualSleepStartClock(String habitualSleepStartClock) {
        this.habitualSleepStartClock = habitualSleepStartClock;
    }

    public String getHabitualWakeUpClock() {
        return habitualWakeUpClock;
    }

    public void setHabitualWakeUpClock(String habitualWakeUpClock) {
        this.habitualWakeUpClock = habitualWakeUpClock;
    }

    public List<String> getProfileTags() {
        return profileTags;
    }

    public void setProfileTags(List<String> profileTags) {
        this.profileTags = profileTags;
    }
}
