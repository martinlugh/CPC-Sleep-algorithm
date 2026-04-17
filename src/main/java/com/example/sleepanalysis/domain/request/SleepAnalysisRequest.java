package com.example.sleepanalysis.domain.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 睡眠分析请求对象。
 */
public class SleepAnalysisRequest {

    /** 分析任务唯一标识。 */
    @NotNull
    private String analysisId;

    /** 用户唯一标识。 */
    @NotNull
    private String userId;

    /** 本次睡眠开始时间。 */
    @NotNull
    private OffsetDateTime sleepSessionStartTime;

    /** 本次睡眠结束时间。 */
    @NotNull
    private OffsetDateTime sleepSessionEndTime;

    /** 输入睡眠片段列表。 */
    @Valid
    @NotEmpty
    private List<SleepSegmentInput> sleepSegmentInputs;

    /** 用户睡眠基线档案。 */
    @Valid
    private UserSleepBaselineProfile userSleepBaselineProfile;

    /** 请求来源系统。 */
    private String requestSourceSystem;

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public OffsetDateTime getSleepSessionStartTime() {
        return sleepSessionStartTime;
    }

    public void setSleepSessionStartTime(OffsetDateTime sleepSessionStartTime) {
        this.sleepSessionStartTime = sleepSessionStartTime;
    }

    public OffsetDateTime getSleepSessionEndTime() {
        return sleepSessionEndTime;
    }

    public void setSleepSessionEndTime(OffsetDateTime sleepSessionEndTime) {
        this.sleepSessionEndTime = sleepSessionEndTime;
    }

    public List<SleepSegmentInput> getSleepSegmentInputs() {
        return sleepSegmentInputs;
    }

    public void setSleepSegmentInputs(List<SleepSegmentInput> sleepSegmentInputs) {
        this.sleepSegmentInputs = sleepSegmentInputs;
    }

    public UserSleepBaselineProfile getUserSleepBaselineProfile() {
        return userSleepBaselineProfile;
    }

    public void setUserSleepBaselineProfile(UserSleepBaselineProfile userSleepBaselineProfile) {
        this.userSleepBaselineProfile = userSleepBaselineProfile;
    }

    public String getRequestSourceSystem() {
        return requestSourceSystem;
    }

    public void setRequestSourceSystem(String requestSourceSystem) {
        this.requestSourceSystem = requestSourceSystem;
    }
}
