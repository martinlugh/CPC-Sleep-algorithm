package com.sleep.platform.domain.request;

import com.sleep.platform.domain.model.UserSleepBaselineProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SleepAnalysisRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;
    @NotNull(message = "分析日期不能为空")
    private LocalDate analysisDate;
    @Valid
    @NotEmpty(message = "生理片段列表不能为空")
    private List<PhysiologicalSegmentInput> physiologicalSegmentList;
    @Valid
    @NotNull(message = "运动片段列表不能为空")
    private List<MotionSegmentInput> motionSegmentList;
    @Valid
    @NotNull(message = "用户基线配置不能为空")
    private UserSleepBaselineProfile baselineProfile;
}
